package de.solugo.gitpal.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author Frederic Kneier
 */
public class RepositoryInfoCache {

    private static final Map<String, RefreshableObjectProperty<RepositoryInfo>> cache = new WeakHashMap<>();

    public static RefreshableObjectProperty<RepositoryInfo> get(final String path) {

        if (cache.containsKey(path)) {
            return cache.get(path);
        } else {
            
            final RefreshableObjectProperty<RepositoryInfo> repositoryInfoProperty = new RefreshableObjectProperty(new RefreshableReadOnlyObjectWrapper<>(() -> {
                return new RepositoryInfo(path);
            }));
            cache.put(path, repositoryInfoProperty);
            return repositoryInfoProperty;
        }
    }

    public static List<RefreshableObjectProperty<RepositoryInfo>> list(final String path) {
        try {
            return Files.walk(Paths.get(path)).filter((p) -> p.endsWith(".git")).map((p) -> get(p.toString())).collect(Collectors.toList());
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
