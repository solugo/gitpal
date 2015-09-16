package de.solugo.gitpal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author Frederic Kneier
 */
public class RepositoryInfo {

    private final String path;
    private Repository repository;
    private String name;
    private Status status;
    private BranchTrackingStatus branchTrackingStatus;
    private Git git;
    private List<String> staged;
    private List<String> unstaged;

    public RepositoryInfo(final String path) {
        this.path = path;
    }

    public Repository getRepository() {
        return this.repository = Util.getDefault(this.repository, () -> {
            return new FileRepository(path);
        });
    }

    public String getName() {
        return this.name = Util.getDefault(this.name, () -> {
            return this.getRepository().getDirectory().getCanonicalFile().getParentFile().getName();
        });
    }

    public Status getStatus() {
        return this.status = Util.getDefault(this.status, () -> {
            return new Git(this.getRepository()).status().call();
        });
    }

    public Map<String, Ref> getTags() {
        final String prefix = "refs/tags/";
        final Map<String, Ref> result = new HashMap<>();
        this.getRepository().getAllRefs().values().stream().filter((ref) -> (ref.getName().startsWith(prefix))).forEach((ref) -> {
            result.put(ref.getName().substring(prefix.length()), ref);
        });
        return Collections.unmodifiableMap(result);
    }

    public Map<String, Ref> getLocalBranches() {
        final String prefix = "refs/heads/";
        final Map<String, Ref> result = new HashMap<>();
        this.getRepository().getAllRefs().values().stream().filter((ref) -> (ref.getName().startsWith(prefix))).forEach((ref) -> {
            result.put(ref.getName().substring(prefix.length()), ref);
        });
        return Collections.unmodifiableMap(result);
    }

    public Map<String, Ref> getRemoteBranches(final String origin) {
        final String prefix = String.format("refs/remotes/%1$s/", origin);
        final Map<String, Ref> result = new HashMap<>();
        this.getRepository().getAllRefs().values().stream().filter((ref) -> (ref.getName().startsWith(prefix))).forEach((ref) -> {
            result.put(ref.getName().substring(prefix.length()), ref);
        });
        return Collections.unmodifiableMap(result);
    }

    public BranchTrackingStatus getBranchTrackingStatus() {
        return this.branchTrackingStatus = Util.getDefault(this.branchTrackingStatus, () -> {
            return BranchTrackingStatus.of(this.getRepository(), this.getRepository().getBranch());
        });
    }

    public Git getGit() {
        return this.git = Util.getDefault(this.git, () -> {
            return new Git(this.getRepository());
        });
    }

    public List<String> getStaged() {
        if (this.staged == null) {
            this.staged = new ArrayList<>();
            this.staged.addAll(getStatus().getAdded());
            this.staged.addAll(getStatus().getChanged());
            this.staged.addAll(getStatus().getRemoved());
        }
        return this.staged;
    }

    public List<String> getUnstaged() {
        if (this.unstaged == null) {
            this.unstaged = new ArrayList<>();
            this.unstaged.addAll(getStatus().getMissing());
            this.unstaged.addAll(getStatus().getModified());
            this.unstaged.addAll(getStatus().getUntracked());
            this.unstaged.addAll(getStatus().getConflicting());
        }
        return this.unstaged;
    }
}
