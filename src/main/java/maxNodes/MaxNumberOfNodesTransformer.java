/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package maxNodes;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;

import com.darylteo.gradle.javassist.transformers.ClassTransformer;


public class MaxNumberOfNodesTransformer extends ClassTransformer {

    public static final String MAXNUMBEROFNODES_FIELD_NAME = "maximumNumberOfNodes";

    private long maxNumberOfNodesValue;

    public MaxNumberOfNodesTransformer(long maxNumberOfNodesValue) {
        this.maxNumberOfNodesValue = maxNumberOfNodesValue;
    }

    @Override
    public void applyTransformations(CtClass clazz) throws JavassistBuildException {
        try {
            if (hasMaximumNumberOfNodesField(clazz)) {
                // replace existing max number of nodes variable
                removeMaxNumberOfNodes(clazz);
                addMaxNumberOfNodes(clazz);
            }
        } catch (Exception e) {
            throw new JavassistBuildException(e);
        }
    }

    private void removeMaxNumberOfNodes(CtClass clazz) throws NotFoundException {
        clazz.removeField(clazz.getField(MAXNUMBEROFNODES_FIELD_NAME));
    }

    private void addMaxNumberOfNodes(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtField field = new CtField(ClassPool.getDefault().get("java.lang.Long"), MAXNUMBEROFNODES_FIELD_NAME, clazz);
        field.setModifiers(javassist.Modifier.PRIVATE | Modifier.STATIC);

        clazz.addField(field, CtField.Initializer.byExpr("Long.valueOf(" + maxNumberOfNodesValue + "L)"));
    }

    @Override
    public boolean shouldTransform(CtClass clazz) throws JavassistBuildException {
        try {
            return isClass(clazz) && isRMCoreClass(clazz);
        } catch (NotFoundException e) {
            throw new JavassistBuildException(e);
        }
    }

    private boolean isClass(CtClass clazz) {
        return !clazz.isInterface() && !clazz.isEnum();
    }

    // Be aware that this functionality is tied to the class name. Any change will break it.
    // But that is better than having the risk to fiddle unwanted with variables which might be called the
    // same, randomly.
    private boolean isRMCoreClass(CtClass clazz) throws NotFoundException {
        return clazz.getName().equals("org.ow2.proactive.resourcemanager.core.RMCore");
    }

    private boolean hasMaximumNumberOfNodesField(CtClass clazz) {
        try {
            CtField serialVersionUIDField = clazz.getField(MAXNUMBEROFNODES_FIELD_NAME);
            return serialVersionUIDField.getDeclaringClass().equals(clazz);
        } catch (NotFoundException classHasNoMaxNumberOfNodes) {
            return false;
        }
    }
}
