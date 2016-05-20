package maxNodes;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.Test;
import maxNodes.tests.Interface;
import maxNodes.tests.RMCore;

import static org.junit.Assert.*;


public class MaxNumberOfNodesTransformerTest {

    @Test
    public void shouldTransform_ClassIsSerializable() throws Exception {
        assertFalse(new MaxNumberOfNodesTransformer(42L).shouldTransform(getClazz(RMCore.class)));

        assertTrue(new MaxNumberOfNodesTransformer(42L)
                .shouldTransform(getClazz(org.ow2.proactive.resourcemanager.core.RMCore.class)));
    }

    @Test
    public void shouldNotTransform_Interface() throws Exception {
        assertFalse(new MaxNumberOfNodesTransformer(42L).shouldTransform(getClazz(Interface.class)));
    }

    private CtClass getClazz(Class clazz) throws NotFoundException {
        return ClassPool.getDefault().get(clazz.getName());
    }
}