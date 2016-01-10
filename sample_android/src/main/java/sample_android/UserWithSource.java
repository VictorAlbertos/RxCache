package sample_android;

import sample_data.entities.User;

/**
 * Created by victor on 09/01/16.
 */
public class UserWithSource {
    private final User user;
    private final String source;

    public UserWithSource(User user, String source) {
        this.user = user;
        this.source = source;
    }

    public User getUser() {
        return user;
    }

    public String getSource() {
        return "Loaded from: " + source;
    }
}
