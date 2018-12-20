import java.io.Serializable;
import java.util.function.Consumer;

interface SerializableConsumer<T> extends Consumer<T>, Serializable {
    /**
     * Used to make sure that Lambda functions are Serializable
     */
}