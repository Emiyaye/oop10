package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import it.unibo.mvc.Configuration.Builder;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private static final String SLASH = File.separator;
    private static final String PATH = "src" + SLASH + "main" + SLASH + "resources" + SLASH + "config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *              the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view : views) {
            view.setObserver(this);
            view.start();
        }

        final Builder builder = new Builder();
        try (BufferedReader br = new BufferedReader(new FileReader(PATH))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                final StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    final String temp = st.nextToken();
                    setbuilder(temp, st.nextToken(), builder);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        this.model = new DrawNumberImpl(builder.build());
    }

    private static void setbuilder(final String command, final String value, final Builder b) {
        if (command.equals("minimum:")) {
            b.setMin(Integer.parseInt(value));
        } else if (command.equals("maximum:")) {
            b.setMax(Integer.parseInt(value));
        } else if (command.equals("attempts:")) {
            b.setAttempts(Integer.parseInt(value));
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view : views) {
                view.result(result);
            }
        } catch (final IllegalArgumentException e) {
            for (final DrawNumberView view : views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *             ignored
     * @throws FileNotFoundException
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl());
    }

}
