package fi.aalto.cs.apluscourses.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class CollectionUtilTest {

  @Test
  public void testMapWithIndex() {
    List<String> source = Arrays.asList("a", "b", "c");
    List<String> result =
        CollectionUtil.mapWithIndex(source, (item, index) -> item + index.toString(), 4);
    assertThat(result, is(Arrays.asList("a4", "b5", "c6")));
  }

  @Test
  public void testIndexOf() {
    Object item = new Object();
    Iterator<Object> it = Arrays.asList(new Object(), new Object(), item, new Object()).iterator();
    assertEquals(2, CollectionUtil.indexOf(it, item));
  }
}
