package maxNodes

import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.testng.AssertJUnit.assertEquals

public class replaceMaxNumberOfNodesTaskTest {

    @Test
    public void testThatMaxOfNumberOfNodesIsReplace() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')


        project.task("maxNodes", type: replaceMaxNumberOfNodesTask.class) {
             // maxNumberOfNodes = '40L'
        }

        createRMCoreClass(project)

        project.compileJava.execute()

        assertEquals(-1L, getCompiledRMCoreClassInPackage(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())

        project.compileJava.execute()
        project.replaceMaxNumberOfNodes.execute()

        assertEquals(40L, getCompiledRMCoreClassInPackage(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())
    }

    @Test
    public void testSameClassInDifferentPackageIsNotChanged() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')

        project.task("replaceMaxNumberOfNodes") {
            // maxNumberOfNodes = '40L'
        }

        createRMCoreWithoutPackage(project)

        project.compileJava.execute()

        assertEquals(-1L, getCompiledRMCoreClassWithoutPackage(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())

        project.compileJava.execute()
        project.replaceMaxNumberOfNodes.execute()

        assertEquals(-1L, getCompiledRMCoreClassWithoutPackage(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())
    }

    @Test(expected = javassist.NotFoundException.class)
    public void testPropertyIsNotAddedToClass() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')

        project.task("replaceMaxNumberOfNodes") {
            // maxNumberOfNodes = '40L'
        }

        createRMCoreWithoutField(project)
        project.compileJava.execute()
        project.replaceMaxNumberOfNodes.execute()

        // This will fail because no property was added.
        assertEquals(-1L, getCompiledRMCoreClassWithoutPackage(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())
    }


    private void createRMCoreWithoutField(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'RMCore.java') <<
                'public class RMCore  {\n' +
                '}'
    }

    private void createRMCoreWithoutPackage(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'RMCore.java') <<
                'public class RMCore  {\n' +
                'private final static long MAXIMUM_NUMBER_OF_NODES = -1;\n' +
                '}'
    }

    private void createRMCoreClass(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'RMCore.java') <<
                'package org.ow2.proactive.resourcemanager.core;\n' +
                'public class RMCore  {\n' +
                'private final static long MAXIMUM_NUMBER_OF_NODES = -1;\n' +
                '}'
    }


    private CtClass getCompiledRMCoreClassWithoutPackage(Project project) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new FileInputStream(new File(project.sourceSets.main.output[0], 'RMCore.class')));
        ctClass
    }

    private CtClass getCompiledRMCoreClassInPackage(Project project) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new FileInputStream(new File(project.sourceSets.main.output[0], '/org/ow2/proactive/resourcemanager/core/RMCore.class')));
        ctClass
    }

}