package fi.aalto.cs.apluscourses.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CollectionUtil {
  private CollectionUtil() {

  }

  /**
   * Index-supporting mapping for lists.
   *
   * @param list Source list.
   * @param func Function from source element and its index to result element.
   * @param startIndex The beginning of indexing.
   * @param <T> Type of source list elements.
   * @param <R> Type of result list elements.
   * @return List of result elements.
   */
  public static <T, R> List<R> mapWithIndex(@NotNull List<T> list,
                                            @NotNull BiFunction<T, Integer, R> func,
                                            int startIndex) {
    return IntStream
        .range(0, list.size())
        .mapToObj(i -> func.apply(list.get(i), startIndex + i))
        .collect(Collectors.toList());
  }

  /**
   *  Returns the index of the given element in an iterator.
   *
   * @param iterator An iterator.
   * @param itemToFind A item that is wanted to be found.
   * @param <T> Type of items.
   * @return The index of the item.
   */
  public static <T> int indexOf(@NotNull Iterator<T> iterator, @Nullable T itemToFind) {
    int index = 0;
    while (iterator.hasNext()) {
      T current = iterator.next();
      if (Objects.equals(current, itemToFind)) {
        return index;
      }
      index++;
    }
    return -1;
  }
}
