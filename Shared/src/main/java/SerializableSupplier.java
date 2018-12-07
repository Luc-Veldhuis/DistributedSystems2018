import java.io.Serializable;
import java.util.function.Supplier;

interface SerializableSupplier<T> extends Supplier<T>, Serializable {
}
