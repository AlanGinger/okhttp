package okhttp3;

import java.io.IOException;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Pipe;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public final class FoldingPipeTest {

  @Test
  public void name() throws IOException, InterruptedException {
    Pipe pipe = new Pipe(128);

    BufferedSink pipeSink = Okio.buffer(pipe.sink());
    pipeSink.writeUtf8("hello");
    pipeSink.flush();

    BufferedSource pipeSource = Okio.buffer(pipe.source());
    assertEquals("hello", pipeSource.readUtf8(5));

    Buffer actualSink = new Buffer();

    fold(pipe, actualSink); // TODO.

    pipeSink.writeUtf8("world");
    pipeSink.flush();
    Thread.sleep(100);
    assertEquals("world", actualSink.readUtf8(5));

    pipeSink.close();
    try {
      pipeSource.readUtf8();
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  private void fold(Pipe pipe, BufferedSink actualSink) {
    Thread thread = new Thread() {
      @Override public void run() {
        try {
          actualSink.writeAll(pipe.source());
          actualSink.close();
        } catch (IOException e) {
          // Be sad? I don't know
        }
      }
    };
    thread.start();
  }
}
