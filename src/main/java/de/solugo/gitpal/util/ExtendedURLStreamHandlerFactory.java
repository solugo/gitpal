package de.solugo.gitpal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

/**
 *
 * @author Frederic Kneier
 */
public class ExtendedURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
        if (protocol.startsWith("classpath")) {
            return new ClasspathURLStreamHandler();
        }
        if (protocol.startsWith("svg")) {
            return new SvgURLStreamHandler();
        }
        if (protocol.startsWith("icon")) {
            return new IconURLStreamHandler();
        }
        if (protocol.startsWith("fxml")) {
            return new FxmlURLStreamHandler();
        }
        return null;
    }

    public static class SvgURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(final URL url) throws IOException {
            final URL targetUrl = new URL(url.toString().substring("svg".length() + 1));
            return new SvgURLConnection(targetUrl);
        }

    }

    public static class ClasspathURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(final URL url) throws IOException {
            return new ClasspathURLConnection(url);
        }
    }

    public static class IconURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(final URL url) throws IOException {
            final URL targetUrl = new URL("svg:classpath:images/" + url.toString().substring("icon".length() + 1));
            return targetUrl.openConnection();
        }
    }

    public static class FxmlURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(final URL url) throws IOException {
            final URL targetUrl = new URL(url.toString().substring("fxml".length() + 1));
            return targetUrl.openConnection();
        }
    }

    public static class ClasspathURLConnection extends URLConnection {

        public ClasspathURLConnection(final URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return ClasspathURLStreamHandler.class.getResourceAsStream("/" + url.getPath());
        }
    }

    public static class SvgURLConnection extends URLConnection {

        public SvgURLConnection(final URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            final URLConnection connection = this.url.openConnection();
            try (final InputStream urlInputStream = connection.getInputStream()) {

                float width = -1;
                float height = -1;
                String color = null;

                final ExtendedImageTranscoder transcoder = new ExtendedImageTranscoder();
                if (url.getQuery() != null) {
                    for (final String queryParameter : url.getQuery().split("&")) {
                        final String[] parts = queryParameter.split("=");
                        if (parts.length == 2) {
                            switch (parts[0]) {
                                case "size": {
                                    width = Float.valueOf(parts[1]);
                                    height = Float.valueOf(parts[1]);
                                    break;
                                }
                                case "width": {
                                    width = Float.valueOf(parts[1]);
                                    break;
                                }
                                case "height": {
                                    height = Float.valueOf(parts[1]);
                                    break;
                                }
                                case "color": {
                                    color = parts[1];
                                    break;
                                }
                            }
                        }
                    }
                }

                if (width != -1) {
                    transcoder.addTranscodingHint(ExtendedImageTranscoder.KEY_WIDTH, (float) width);
                }
                if (height != -1) {
                    transcoder.addTranscodingHint(ExtendedImageTranscoder.KEY_HEIGHT, (float) height);
                }
                if (color != null) {
                    transcoder.addTranscodingHint(ExtendedImageTranscoder.KEY_COLOR, color);
                }

                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                transcoder.transcode(new TranscoderInput(urlInputStream), new TranscoderOutput(outputStream));

                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (final IOException | TranscoderException exception) {
                throw new RuntimeException(exception);
            }

        }

    }

}
