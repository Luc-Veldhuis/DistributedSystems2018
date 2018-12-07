import java.io.Serializable;
import java.util.function.Supplier;

interface SerializableSupplier<T> extends Supplier<T>, Serializable {
    /**
     * Used to make sure that Lambda functions are Serializable
     */
}
