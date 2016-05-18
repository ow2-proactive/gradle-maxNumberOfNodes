package maxNodes

import com.darylteo.gradle.javassist.tasks.TransformationTask
import org.gradle.api.tasks.TaskAction

public class replaceMaxNumberOfNodesTask extends TransformationTask {

    def maxNumberOfNodes

    replaceMaxNumberOfNodesTask() {
        dependsOn(project.classes)
        project.jar.mustRunAfter(this)
    }

    @TaskAction
    public void exec() {
        classpath += project.configurations.compile
        def maximumNumberOfNodes
        if (maxNumberOfNodes instanceof String) {
            maximumNumberOfNodes = Long.parseLong(maxNumberOfNodes.replaceAll('L', '').replaceAll('l', ''))
        } else {
            maximumNumberOfNodes = maxNumberOfNodes
        }

        setTransformation(new MaxNumberOfNodesTransformer(maximumNumberOfNodes))

        // in place transformation
        from(project.sourceSets.main.output[0])
        into(project.sourceSets.main.output[0])

        super.exec()
    }

}
