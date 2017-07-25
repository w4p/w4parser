package test.result;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestResult<T> {

    private T result = null;

}
