package sample_android;

import sample_data.entities.Repo;

/**
 * Created by victor on 09/01/16.
 */
public class RepoWithSource {
    private final Repo repo;
    private final String source;

    public RepoWithSource(Repo repo, String source) {
        this.repo = repo;
        this.source = source;
    }

    public Repo getRepo() {
        return repo;
    }

    public String getSource() {
        return "Loaded from: " + source;
    }
}
