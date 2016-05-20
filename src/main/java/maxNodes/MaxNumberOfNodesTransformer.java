/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
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
        field.setModifiers( javassist.Modifier.PRIVATE | Modifier.STATIC );

        clazz.addField(field, CtField.Initializer.byExpr("Long.valueOf("+maxNumberOfNodesValue+"L)"));
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
