package zombiecat.client.module.modules.bannable;

import com.google.common.collect.Lists;
import net.minecraft.util.BlockPos;
import org.apache.commons.io.IOUtils;
import zombiecat.client.module.Module;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class MapGB extends Module {

   public MapGB() {
      super("MapGB", ModuleCategory.bannable);
   }

   @Override
   public void onEnable() {
      File file = new File(mc.mcDataDir, "gb.txt");
      List<BlockPos> gbList = Lists.newArrayList();
      if (file.exists()) {
         try {
            for (String s : IOUtils.readLines(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
               String[] split = s.split(",");
               List<Double> list = Lists.newArrayList();
               for (String a : split) {
                  try {
                     list.add(Double.parseDouble(a));
                  } catch (Exception e) {
                     break;
                  }
               }
               try {
                  gbList.add(new BlockPos(list.get(0), list.get(1), list.get(2)));
               } catch (Exception e) {
               }
            }
            for (BlockPos bp : gbList) {
               mc.theWorld.setBlockToAir(bp);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         try {
            file.createNewFile();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      this.disable();
   }
}
