package lsa.viewercloudpoints.filechooser;


/**
 * Created by Luan Sala on 11/03/2015.
 */
public class OptionFile implements Comparable<OptionFile> {

    private String nameFile;
    private String info;
    private boolean folder;

    public OptionFile(String nameFile,String info,boolean fd) {
        this.nameFile = nameFile;
        this.info = info;
        folder = fd;
    }

    public String getName() {
        return nameFile;
    }

    public String getInfo() {
        return info;
    }

    public boolean isDirectory() {
        return folder;
    }

    public boolean isFile() {
        return !folder;
    }

    @Override
    public int compareTo(OptionFile another) {
        return this.nameFile.toLowerCase().compareTo( another.getName().toLowerCase() );
    }

    @Override
    public String toString() {
        return nameFile;
    }

}
