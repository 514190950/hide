import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.*;

public class BilibiliOut {
    public static void main(String[] args) throws IOException {
        File mvFile=new File("C:\\Users\\Administrator\\AppData\\Local\\Packages\\36699Atelier39.forWin10_pke1vz55rvc1r\\LocalCache\\BilibiliDownload\\25715668");
       Map<String,List<File>> output=new HashMap<>();
        for (File file : mvFile.listFiles()) {
             if(file.isDirectory()){
                 output.putAll(BilibiliOut.getMvName1(file));
             }
        }

      BilibiliOut.outPutBilibili(output,getDirectoryName(mvFile));
        deleteAllFiles(mvFile);




    }
    public static Map<String,List<File>> getMvName1(File file) throws IOException {//传入的是一个有信息文件和视频的文件夹
          String mvName="";
          List<File> fileList=new ArrayList<>();
          if(file.isDirectory()){
              for(File file1:file.listFiles()){
                    if(file1.getName().contains(".info")){
                       mvName = BilibiliOut.getMvName(file1);  //先找信息文件 获取名称
                    }else if(file1.getName().contains(".flv")){
                        fileList.add(file1);
                    }
              }
              Map<String,List<File>> result=new HashMap<>();
              result.put(mvName,fileList);
              return result;
          }else{
              return null;
          }
    }
    public static String getMvName(File info) throws IOException {
        if(info.isDirectory()){
           return null;
        }
        StringBuilder sb=new StringBuilder();
        BufferedReader fis=null;
        String result="";
          try {
              fis=new BufferedReader(new FileReader(info));
              String str="";
              while ((str=fis.readLine())!=null){
                    sb.append(str);
              }
              Map<String,String> infoMap = (Map) JSONObject.parse(sb.toString());
               result=infoMap.get("PartName");
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          }finally {
              fis.close();
              return result;
          }
    }
    public static void outPutBilibili(Map<String,List<File>>  map,String deName) throws IOException {
        Set<String> keySet = map.keySet();
         String name ="";

        for (String key : keySet) {
            name=key;
            List<File> files = map.get(key);
            if(files.size()>1){
                for(int i=1;i<=files.size();i++){
                    name+=i;
                    BilibiliOut.outPutMV(name,files.get(i-1),deName);
                }
            }else{
                BilibiliOut.outPutMV(name,files.get(0),deName);
            }

        }

    }
    public static void outPutMV(String fileName,File file,String derName) throws IOException {
        fileName+=".flv";
        File de=new File("E:\\教程\\B站视频"+"\\"+derName);
        if(!de.exists()){
            de.mkdir();
        }

        File outFile=new File("E:\\教程\\B站视频"+"\\"+derName+"\\"+fileName);
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(file);
            output = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int bytesRead;
            long start = System.currentTimeMillis();
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
                    }
                long end = System.currentTimeMillis();
		    System.out.println("复制了文件:"+fileName+"运行时间："+(end-start)+"毫秒");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static String getDirectoryName(File file) throws IOException {
        File divFile=null;
        for (File listFile : file.listFiles()) {
                if(listFile.getName().contains(".dvi")){
                    divFile=listFile;
                    break;
                }
        }
         StringBuilder sb=new StringBuilder();
        BufferedReader fis=null;
        String div="";
         try {
             fis=new BufferedReader(new FileReader(divFile));
             String str="";
             while ((str=fis.readLine())!=null){
                        sb.append(str);
             }
             Map<String,String> map = (Map)JSONObject.parse(sb.toString());
             div=map.get("Title");

         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             fis.close();
         }
        return div;
    }
    public static void deleteAllFiles(File file){
             if(file == null || !file.exists())
                       return ;
                if(file.isDirectory()){
                    File[] files = file.listFiles();
                     if(files != null){
                         for(File f : files)
                                deleteAllFiles(f);
                         }
                 }

        System.out.println("删除:"+file.getName()+":"+file.delete());
          }
}
