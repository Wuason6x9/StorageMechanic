package dev.wuason.storagemechanic.actions.utils;

public class GetInstances {
    /**
     * Returns an instance from the given array of objects that is of the same class or a subclass of the specified object.
     *
     * @param object The object to match against.
     * @param args   An array of objects to search through.
     * @return The first instance from the array that is of the same class or a subclass of the specified object, or null if no matching instance is found.
     */
    public static Object getInstance(Object object, Object[] args) {
        for (Object arg : args) {
            if (arg.getClass().isInstance(object)) {
                return arg;
            }
        }

        return null;
    }
}
