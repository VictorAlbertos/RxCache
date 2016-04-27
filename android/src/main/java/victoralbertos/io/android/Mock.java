package victoralbertos.io.android;

/**
 * Created by victor on 27/04/16.
 */
public class Mock {
    final private String message;

    public Mock(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public class InnerMock {
        final private String message;

        public InnerMock(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}