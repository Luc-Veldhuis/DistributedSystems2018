import java.io.Serializable;
import java.util.function.Consumer;

interface SerializableConsumer<T> extends Consumer<T>, Serializable {}