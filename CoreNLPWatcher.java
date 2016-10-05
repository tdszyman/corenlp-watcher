/*
 * Terrence Szymanski
 * terrence.szymanski@insight-centre.org
 * 6 May 2015
 *
 * This is a simple application to run Stanford CoreNLP as a
 * daemon. This daemon watches a directory for new .txt files;
 * whenever a new .txt file appears, it is piped into the CoreNLP
 * pipeline. The CoreNLP output is saved as an .xml file in the same
 * directory along side the .txt input.
 *
 * This code was written while reading the tutorial below, and may
 * contain some snippets of code borrowed from there:
 * https://docs.oracle.com/javase/tutorial/essential/io/notification.html
 *
 * Additionally, the Stanford CoreNLP code was written after looking
 * at the StanfordCoreNlpDemo.java file included in the CoreNLP
 * distribution, and may contain some similarities to that code.
 *
 * For suggestions on how this code could be modified to improve
 * performance time, particularly on Macs, see this post:
 * http://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else
 *
 */


/* WATCHER IMPORTS */
import java.io.IOException;
import java.nio.file.FileSystems;    
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/* STANFORD CORELNP IMPORTS */
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import java.io.PrintWriter;
import java.util.Properties;



public class CoreNLPWatcher {

    /* The directory to be watched */
    private final Path watchDir;
    /* A single instance of WatchService to watch the directory */
    private final WatchService watcher;
    /* A single instance of a CoreNLP pipeline to analyze the texts */
    private final StanfordCoreNLP pipeline;

    /*
     * Convenience method to cast WatchEvent to Path and suppress
     * warning message.
     */
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    /* 
     * This is the main loop to watch a directory and handle file
     * events, passing new .txt files to the annotation pipeline.
     */
    public void watch() throws IOException {
        System.out.format("Watching directory %s\n", watchDir);
        while(true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                // I'm not exactly sure what this exception is.
                break;
            }
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW) {
                    // Should do something here? Not sure what.
                    System.out.println("OVERFLOW event detected.");
                } else if (kind == ENTRY_CREATE) {
                    WatchEvent<Path> ev = cast(event);
                    Path filename = watchDir.resolve(ev.context());
                    System.out.format("ENTRY_CREATE event detected: %s\n", filename);
                    if (filename.toString().toLowerCase().endsWith(".txt")) {
                        System.out.format("Annotating new file '%s' with CoreNLP.\n", filename);
                        annotateFile(filename);
                    }
                } else {
                    // Do nothing
                }
            }
            key.reset();
        }
    }

    /*
     * Annotate a file using the CoreNLP pipeline.
     */
    public void annotateFile(Path filename) throws IOException {

        // Read text from file
        String textFilename = filename.toString();
        System.out.format(" * Reading text from file %s ... ", textFilename);
        Annotation annotation = new Annotation(IOUtils.slurpFileNoExceptions(textFilename));
        System.out.format("read %d characters.\n", annotation.toString().length());

        // Annotate the text
        System.out.print(" * Annotating text... ");
        pipeline.annotate(annotation);
        Integer t = annotation.get(CoreAnnotations.TokensAnnotation.class).size();
        Integer s = annotation.get(CoreAnnotations.SentencesAnnotation.class).size();
        System.out.format("annotated %d tokens in %d sentences.\n", t, s);

        // Write annotation to XML file
        String xmlFilename = textFilename + ".xml";
        System.out.format(" * Writing xml to file %s ... ", xmlFilename);
        PrintWriter out = new PrintWriter(xmlFilename);
        pipeline.xmlPrint(annotation, out);
        System.out.println("Done.");

        System.out.println("Done.");
        return;
    }

    /*
     * CoreNLPWatcher Constructor. This instantiates the watcher and
     * the CoreNLP annotator.
     */
    CoreNLPWatcher(Path watchDir) throws IOException {

        // First, set up the watcher on the given directory.
        System.out.println("Creating watch service...");
        this.watchDir = watchDir;
        this.watcher = FileSystems.getDefault().newWatchService();
        try {
            WatchKey key = this.watchDir.register(this.watcher, ENTRY_CREATE);
        } catch (IOException e) {
            System.err.println("ERROR: Unable to register watch directory.");
            System.err.println(e);
            System.exit(1);
        }

        // Next, set up the CoreNLP pipeline.
        System.out.println("Creating Stanford CoreNLP pipeline...");
        Properties props = new Properties();
        // These should be settable from the command line. 
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner, depparse");
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse, parse, dcoref");
        props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
        props.put("ner.applyNumericClassifiers", "false");
        //props.put("regexner.mapping","regexner.txt");
        this.pipeline = new StanfordCoreNLP(props);

        System.out.println("All done! CoreNLPWatcher initialized.");

    }

    /*
     * Main function. Processes arguments, instantiates a
     * CoreNLPWatcher, and and calls the watcher's watch() function.
     */
    public static void main(String[] args) throws IOException {

        // This should be replaced by some kind of argument parsing.
        Path watchDir = Paths.get(args[0]);

        // Display a welcome message.
        System.out.println("Welcome to CoreNLPWatcher!");

        // Run the main loop. This should probably go inside of some
        // kind of exception handling.
        CoreNLPWatcher watcher = new CoreNLPWatcher(watchDir);
        watcher.watch();

    }

}
