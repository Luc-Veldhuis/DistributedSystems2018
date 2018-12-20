import java.io.Serializable;
import java.util.function.Function;

interface SerializableFunction<T,K> extends Function<T, K>, Serializable {
    /**
     * Used to make sure that Lambda functions are Serializable
     */
}