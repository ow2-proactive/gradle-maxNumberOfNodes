package maxNodes

import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import java.lang.reflect.Field

import static org.testng.AssertJUnit.assertEquals

public class replaceMaxNumberOfNodesTaskTest {

    @Test
    public void testThatMaxOfNumberOfNodesIsNullWithoutBeingReplace() throws Exception {
        Project projectVariableNotReplaced = ProjectBuilder.builder().build()
        projectVariableNotReplaced.apply(plugin: 'java')

        createRMCoreClass(projectVariableNotReplaced)

        projectVariableNotReplaced.compileJava.execute()

        assertThatMaxNumberOfNodesIs(null, getCompiledRMCoreClassInPackage(projectVariableNotReplaced))
    }

    @Test
    public void testThatMaxOfNumberOfNodesIsReplacedStringValue() throws Exception {
        Project projectVariableReplaced = ProjectBuilder.builder().build()
        projectVariableReplaced.apply(plugin: 'java')
        projectVariableReplaced.task("replaceMaxNumberOfNodes", type: ReplaceMaxNumberOfNodesTask.class) {
            maxNumberOfNodes = '40L'
        }

        createRMCoreClass(projectVariableReplaced)

        projectVariableReplaced.compileJava.execute()
        projectVariableReplaced.replaceMaxNumberOfNodes.execute()

        assertThatMaxNumberOfNodesIs(40L, getCompiledRMCoreClassInPackage(projectVariableReplaced))
    }

    private void assertThatMaxNumberOfNodesIs(Long expectedValue, CtClass forClass) {
        assertEquals(expectedValue, getMaxNumberOfNodesField(forClass).get(null))
    }

    private Field getMaxNumberOfNodesField(CtClass RMCoreClass) {
        ClassLoader classLoader = new URLClassLoader()
        Field maxNumberOfNodesField = RMCoreClass.toClass(classLoader, null).getDeclaredField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME)
        maxNumberOfNodesField.setAccessible(true)

        RMCoreClass.defrost()

        return maxNumberOfNodesField
    }

    @Test
    public void testThatMaxOfNumberOfNodesIsReplacedIntValue() throws Exception {
        Project projectVariableReplaced = ProjectBuilder.builder().build()
        projectVariableReplaced.apply(plugin: 'java')
        projectVariableReplaced.task("replaceMaxNumberOfNodes", type: ReplaceMaxNumberOfNodesTask.class) {
            maxNumberOfNodes = 40
        }

        createRMCoreClass(projectVariableReplaced)

        projectVariableReplaced.compileJava.execute()
        projectVariableReplaced.replaceMaxNumberOfNodes.execute()

        assertThatMaxNumberOfNodesIs(40L, getCompiledRMCoreClassInPackage(projectVariableReplaced))
    }

    @Test
    public void testSameClassInDifferentPackageIsNotChanged() throws Exception {
        Project projectVariableReplaced = ProjectBuilder.builder().build()
        projectVariableReplaced.apply(plugin: 'java')
        projectVariableReplaced.task("replaceMaxNumberOfNodes", type: ReplaceMaxNumberOfNodesTask.class) {
            maxNumberOfNodes = 40
        }

        createRMCoreWithoutPackage(projectVariableReplaced)

        projectVariableReplaced.compileJava.execute()
        projectVariableReplaced.replaceMaxNumberOfNodes.execute()

        assertThatMaxNumberOfNodesIs(null, getCompiledRMCoreClassWithoutPackage(projectVariableReplaced))
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
                'private static Long maximumNumberOfNodes;\n' +
                '}'
    }

    private void createRMCoreClass(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'RMCore.java') <<
                'package org.ow2.proactive.resourcemanager.core;\n' +
                'public class RMCore  {\n' +
                'private static Long maximumNumberOfNodes;\n' +
                '}'
    }


    private CtClass getCompiledRMCoreClassWithoutPackage(Project project) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new FileInputStream(new File(project.sourceSets.main.output[0], 'RMCore.class')));
        ctClass
    }

    private CtClass getCompiledRMCoreClassInPackage(Project project) {
        System.out.println(project.sourceSets.main.output[0].toString())
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new FileInputStream(new File(project.sourceSets.main.output[0], '/org/ow2/proactive/resourcemanager/core/RMCore.class')));
        ctClass
    }

}