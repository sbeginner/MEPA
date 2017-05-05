package FileIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static Setup.Config.CHARSETTYPE;
import static Setup.Config.CURRENT_TIME;
import static Setup.Config.FILEPATH;

/**
 * Created by JACK on 2017/5/4.
 */
public class DataOutput{

    private boolean currentMode = false;
    private int currentFoldValid = 0;
    private Path outputPath;
    private String outputRootPath = FILEPATH+"/output_"+CURRENT_TIME;
    private String outputTrainPath = outputRootPath+"/train/";
    private String outputTestPath = outputRootPath+"/test/";
    private Charset charset = Charset.forName(CHARSETTYPE);

    public DataOutput(boolean currentMode, int currentFoldValid){
        this.currentMode = currentMode;
        this.currentFoldValid = currentFoldValid;

        checkOutputDirIsExist(outputRootPath);
        checkOutputDirIsExist(outputTrainPath);
        checkOutputDirIsExist(outputTestPath);
    }

    private void checkOutputDirIsExist(String dirPath){
        File outputRootDir = new File(dirPath);
        if(!outputRootDir.exists()){
            outputRootDir.mkdir();
        }
    }

    private void outputDataProcess(StringBuilder MembershipNameStr, Path outputPath){
        BufferedWriter outputBuffer;

        try {
            outputBuffer = Files.newBufferedWriter(outputPath, charset);
            outputBuffer.write(MembershipNameStr.toString());
            outputBuffer.flush();
            outputBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outputTrainMembership(StringBuilder MembershipNameStr, String fileName){
        String outputTrainPathtmp;
        if(currentMode){
            outputTrainPathtmp = outputTrainPath+"/";
        }else{
            outputTrainPathtmp = outputTrainPath+"/Fold"+currentFoldValid+"/";
        }

        checkOutputDirIsExist(outputTrainPathtmp);
        outputPath = Paths.get(outputTrainPathtmp, fileName+".txt");
        outputDataProcess(MembershipNameStr, outputPath);
    }

    public void outputTestMembership(StringBuilder MembershipNameStr, String fileName){
        String outputTestPathtmp;
        if(currentMode){
            outputTestPathtmp = outputTestPath+"/";
        }else{
            outputTestPathtmp = outputTestPath+"/Fold"+currentFoldValid+"/";
        }
        checkOutputDirIsExist(outputTestPathtmp);
        outputPath = Paths.get(outputTestPathtmp,  fileName+".txt");
        outputDataProcess(MembershipNameStr, outputPath);
    }

}
