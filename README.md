# corenlp-watcher
Watch a directory and process new text files with Stanford CoreNLP

CoreNLP-Watcher is a small Java application that watches a given
directory and automatically processses any new text files that appear
there, using the Stanford CoreNLP tools. This is may be useful if you
want to quckly process new text files as they appear in real time (for
example, when running a web application) without the overhead of
loading the CoreNLP models each time you process a new file, and/or if
you want to utilize CoreNLP from an application that is not written in
Java, using a simple file-based protocol.

## Requirements

You need to have the Stanford CoreNLP version 3.5.1 (<a
href="http://nlp.stanford.edu/software/stanford-corenlp-full-2015-01-29.zip"
target="_blank">download link</a>) downloaded and unpacked somewhere
on your computer.

Note: CoreNLP version 3.6.0 introduced a built-in API server, which
largely renders CoreNLP-Watcher obsolete.

## Installation and Usage

If do not have Stanford CoreNLP installed already, download and unzip
the package somewhere on your computer.

Edit the `settings` file with the full path to the CoreNLP files, as
well as the full path to the directory to be watched.

To compile the CoreNLPWatcher Java code, use the provided
Makefile. Note that this requries that the path to CoreNLP be
specified in an environment variable, which can be done by sourcing
the `settings` file.

    source settings
    make

Launch CoreNLPWatchr by executing the `corenlpw` script. You will need
to wait a minute or so for the CoreNLP models to load into memory, and
then the watcher will automatically process any new files that appear
in the watch direct.

    ./corenlpw &

For example, if the watcher is running and watching the
`/tmp/corenlpw` directory, you can test it like so:

    echo "This is a test." > /tmp/corenlpw/test.txt

Which should result in a file `watch/test.txt.xml` containing the
CoreNLP annotation output.

## License

As it incorporates the Stanford CoreNLP code, this software is
licensed under the GNU General Public License.

## Credits

By Terrence Szymanski. <a
href="http://www.affrication.org">www.affrication.org</a>