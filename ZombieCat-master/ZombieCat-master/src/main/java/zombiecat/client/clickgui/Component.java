package zombiecat.client.clickgui;

public interface Component {
   void draw();

   void update(int var1, int var2);

   void mouseDown(int var1, int var2, int var3);

   void mouseReleased(int var1, int var2, int var3);

   void keyTyped(char var1, int var2);

   void setComponentStartAt(int var1);

   int getHeight();
}
