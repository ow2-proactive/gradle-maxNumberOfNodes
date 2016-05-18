package maxNodes

import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Ignore
import org.junit.Test

import static org.testng.AssertJUnit.assertEquals

public class replaceMaxNumberOfNodesTaskTest {

    @Ignore
    @Test
    public void shouldReplaceMaxOfNumberOfNodes() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')

        project.task("replaceMaxNumberOfNodes", type: replaceMaxNumberOfNodesTask.class) {
            maxNumberOfNodes = '40L'
        }

        createSerializableJavaSource(project)
        createExceptionJavaSource(project)

        project.compileJava.execute()

        assertEquals(-1L, getCompiledSerialzableClass(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())

        project.compileJava.execute()
        project.replaceMaxNumberOfNodes.execute()

        assertEquals(40L, getCompiledSerialzableClass(project).getField(MaxNumberOfNodesTransformer.MAXNUMBEROFNODES_FIELD_NAME).getConstantValue())
    }




    private void shouldUpdateSerialVersionUIDField(boolean overwrt, boolean forcethrow, long expectedUIDSerializable, long expectedUIDException) {
        Project project = ProjectBuilder.builder().build()
        project.apply(plugin: 'java')

        project.task("maxNumberOfNodes", type: replaceMaxNumberOfNodesTask.class) {
            serialver = '42L'
            overwrite = overwrt
            forceUIDOnException = forcethrow
        }

        createSerializableJavaSourceWithSerialVersionUID(project)
        createExceptionJavaSourceWithSerialVersionUID(project)

        project.compileJava.execute()
        project.serialver.execute()

        assertEquals(expectedUIDSerializable, getCompiledSerialzableClass(project).getField("serialVersionUID").getConstantValue())
        assertEquals(expectedUIDException, getCompiledExceptionClass(project).getField("serialVersionUID").getConstantValue())
    }


    private void createSerializableJavaSource(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'SerializableClass.java') <<
                'import java.io.Serializable;\n' +
                'public class SerializableClass implements Serializable {\n' +
                '}'
    }

    private void createExceptionJavaSource(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'ExceptionClass.java') <<
                'public class ExceptionClass extends Exception {\n' +
                '}'
    }

    private void createSerializableJavaSourceWithSerialVersionUID(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'SerializableClass.java') <<
                'import java.io.Serializable;\n' +
                'public class SerializableClass implements Serializable {\n' +
                'private static final long serialVersionUID = 7L; \n' +
                '}'
    }

    private void createExceptionJavaSourceWithSerialVersionUID(Project project) {
        FileUtils.forceMkdir(project.sourceSets.main.java.srcDirs[0])

        new File(project.sourceSets.main.java.srcDirs[0], 'ExceptionClass.java') <<
                'public class ExceptionClass extends Exception {\n' +
                'private static final long serialVersionUID = 7L; \n' +
                '}'
    }

    private CtClass getCompiledSerialzableClass(Project project) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new FileInputStream(new File(project.sourceSets.main.output[0], 'SerializableClass.class')));
        ctClass
    }

    private CtClass getCompiledExceptionClass(Project project) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new FileInputStream(new File(project.sourceSets.main.output[0], 'ExceptionClass.class')));
        ctClass
    }
}