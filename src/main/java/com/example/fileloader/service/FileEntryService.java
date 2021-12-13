package com.example.fileloader.service;

import com.example.fileloader.dao.FileDataDao;
import com.example.fileloader.dao.FileEntryDao;
import com.example.fileloader.model.FileData;
import com.example.fileloader.model.FileEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * @author masoud
 */
@Service
public class FileEntryService extends BaseService<FileEntry> {

    private final FileEntryDao fileEntryDao;

    @Autowired
    private FileDataDao fileDataDao;

    @Autowired
    public <E extends Object> FileEntryService(FileEntryDao dao) {
        super(dao);
        this.fileEntryDao = dao;
    }

    public FileDataInputStream getInputStream(String id) {
        return new FileDataInputStream(fileEntryDao.findById(id).get().getFileDataId());
    }

    public FileDataOutputStream getOutputStream(String id) {
        return new FileDataOutputStream(fileEntryDao.findById(id).get().getFileDataId());
    }

    @Override
    public FileEntry create(FileEntry file) {
        FileData fileData = new FileData();
        fileData.setData(new byte[]{});
        fileData = fileDataDao.save(fileData);
        file.setFileDataId(fileData.getId());
        file = fileEntryDao.save(file);
        return file;
    }

    @Override
    public void delete(String id) {
        Optional<FileEntry> fileEntryOptional = fileEntryDao.findById(id);
        if (fileEntryOptional.isPresent()) {
            Optional<FileData> masterRecord = fileDataDao.findById(fileEntryOptional.get().getFileDataId());
            if (masterRecord != null && masterRecord.isPresent()) {
                Scanner sc = new Scanner(masterRecord.get().getData() == null ? "" : new String(masterRecord.get().getData()));
                List<FileData> records = new ArrayList<>();
                while (sc.hasNext()) {
                    FileData d = new FileData();
                    d.setId(sc.nextLine());
                    records.add(d);
                }
                records.add(masterRecord.get());
                fileDataDao.deleteAll(records);
            }
            fileEntryDao.deleteById(id);
        }
    }

    public FileEntry findByName(String name) {
        return fileEntryDao.findByName(name);
    }

    public class FileDataInputStream extends InputStream {

        private String id;
        private ByteArrayInputStream is = new ByteArrayInputStream(new byte[]{});
        private List<String> idList = new ArrayList<>();
        private int idIndex = 0;

        public FileDataInputStream(String id) {
            this.id = id;
            FileData master = fileDataDao.findById(id).get();
            Scanner sc = new Scanner(master.getData() == null ? "" : new String(master.getData()));
            while (sc.hasNext()) {
                idList.add(sc.nextLine());
            }
        }

        @Override
        public int read() throws IOException {
            byte b[] = new byte[1];
            int c = read(b, 0, b.length);
            return (c == 1) ? b[0] & 0xff : c;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (is.available() >= len) {
                return is.read(b, off, len);
            } else {
                int cnt = is.available();
                is.read(b, off, cnt);
                off += cnt;
                len -= cnt;
                while (true) {
                    Optional<FileData> data = (idIndex >= idList.size()) ? Optional.empty() : fileDataDao.findById(idList.get(idIndex));
                    idIndex++;
                    if (data.isPresent()) {
                        byte[] dataBytes = data.get().getData();
                        if (len <= dataBytes.length) {
                            System.arraycopy(dataBytes, 0, b, off, len);
                            is = new ByteArrayInputStream(dataBytes, len, dataBytes.length - len);
                            return cnt + len;
                        } else {
                            System.arraycopy(dataBytes, 0, b, off, dataBytes.length);
                            return cnt + dataBytes.length;
                        }
                    } else {
                        return cnt == 0 ? -1 : cnt;
                    }
                }
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
    }

    public class FileDataOutputStream extends OutputStream {

        private String id;
        private ByteArrayOutputStream os = new ByteArrayOutputStream();
        private long length = 0;
        private boolean closed = false;
        private StringBuilder sb = new StringBuilder();

        public FileDataOutputStream(String id) {
            this.id = id;
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (closed) {
                return;
            }
            os.write(b, 0, len);
            length += len;
            flushHelper(false);
        }

        @Override
        public void flush() throws IOException {
            flushHelper(false);
        }

        @Override
        public void close() throws IOException {
            flushHelper(true);
            closed = true;
        }

        private void flushHelper(boolean finish) {
            if (closed) {
                return;
            }
            if (finish || os.size() >= FileData.MAX_SIZE) {
                byte[] bytes = os.toByteArray();
                os.reset();
                for (int i = 0; i < bytes.length / FileData.MAX_SIZE; i++) {
                    byte buf[] = new byte[FileData.MAX_SIZE];
                    System.arraycopy(bytes, i * FileData.MAX_SIZE, buf, 0, buf.length);
                    writeToDB(buf);
                }
                if (bytes.length % FileData.MAX_SIZE != 0) {
                    if (finish) {
                        byte buf[] = new byte[bytes.length % FileData.MAX_SIZE];
                        System.arraycopy(bytes, (bytes.length / FileData.MAX_SIZE) * FileData.MAX_SIZE, buf, 0, buf.length);
                        writeToDB(buf);
                    } else {
                        os.write(bytes, (bytes.length / FileData.MAX_SIZE) * FileData.MAX_SIZE, bytes.length % FileData.MAX_SIZE);
                    }
                }
            }
        }

        private void writeToDB(byte[] buf) {
            FileData newData = new FileData();
            newData.setData(buf);
            fileDataDao.save(newData);
            FileData master = new FileData();
            master.setId(id);
            sb.append("" + newData.getId() + "\n");
            master.setData(sb.toString().getBytes());
            fileDataDao.save(master);
        }

        public Long length() {
            return length;
        }
    }
}
