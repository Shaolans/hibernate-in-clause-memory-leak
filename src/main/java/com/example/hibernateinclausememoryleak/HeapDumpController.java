package com.example.hibernateinclausememoryleak;

import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HeapDumpController {

    private final JdbcTemplate jdbcTemplate;

    private static final UUID id = UUID.randomUUID();

    @GetMapping("/memoryDump")
    public ResponseEntity<Void> createHeapDump() throws IOException {
        InputStream inputStream = null;
        File dumpFile = null;
        File dumpFileGzip = null;
        try {
            HotSpotDiagnosticMXBean diagnosticMXBean = ManagementFactory.getPlatformMXBeans(HotSpotDiagnosticMXBean.class).stream().findFirst().get();
            String prefix = String.format("heapdump-%d", System.currentTimeMillis());
            dumpFile = File.createTempFile(prefix, ".hprof");
            dumpFile.delete();
            log.info(String.format("Dumping file to %s", dumpFile.getAbsolutePath()));
            diagnosticMXBean.dumpHeap(dumpFile.getAbsolutePath(), true);
            dumpFileGzip = File.createTempFile(prefix, ".hprof.gz");
            compressGzipFile(dumpFile, dumpFileGzip);
            inputStream = new FileInputStream(dumpFileGzip);
            InputStream finalInputStream = inputStream;
            jdbcTemplate.update("INSERT INTO public.heapdump (uid, \"timestamp\", dump_binary) VALUES (?,?,?)", preparedStatement -> {
                preparedStatement.setString(1, id.toString());
                preparedStatement.setTimestamp(2, Timestamp.from(ZonedDateTime.now().toInstant()));
                preparedStatement.setBinaryStream(3, finalInputStream);
            });
            log.info("Heap dumped in database");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            dumpFile.delete();
            dumpFileGzip.delete();
        }
        return ResponseEntity.ok().build();
    }

    public static void compressGzipFile(File sourceFile, File compressedFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(compressedFile);
        GZIPOutputStream gzipOS = new GZIPOutputStream(fos);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            gzipOS.write(buffer, 0, bytesRead);
        }

        gzipOS.close();
        fos.close();
        fis.close();
    }
}

