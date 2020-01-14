import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class HelloTest {
    @Test
    public void testCoverage() {
       Assertions.assertEquals(new Hello().say(), "world");
    }
}