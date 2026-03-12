package zombiecat.client.utils;

public class Timer {
   private final float updates;
   private long last;
   private float cached;

   public Timer(float updates) {
      this.updates = updates;
   }

   public float getValueFloat(float begin, float end, int type) {
      if (this.cached == end) {
         return this.cached;
      } else {
         float t = (float)(System.currentTimeMillis() - this.last) / this.updates;
         switch (type) {
            case 1:
               t = t < 0.5F ? 4.0F * t * t * t : (t - 1.0F) * (2.0F * t - 2.0F) * (2.0F * t - 2.0F) + 1.0F;
               break;
            case 2:
               t = (float)(1.0 - Math.pow((double)(1.0F - t), 5.0));
               break;
            case 3:
               t = this.bounce(t);
         }

         float value = begin + t * (end - begin);
         if (end < value) {
            value = end;
         }

         if (value == end) {
            this.cached = value;
         }

         return value;
      }
   }

   public int getValueInt(int begin, int end, int type) {
      return Math.round(this.getValueFloat((float)begin, (float)end, type));
   }

   public void start() {
      this.cached = 0.0F;
      this.last = System.currentTimeMillis();
   }

   private float bounce(float t) {
      double i2 = 7.5625;
      double i3 = 2.75;
      if ((double)t < 1.0 / i3) {
         return (float)(i2 * (double)t * (double)t);
      } else if ((double)t < 2.0 / i3) {
         float var8;
         return (float)(i2 * (double)(var8 = (float)((double)t - 1.5 / i3)) * (double)var8 + 0.75);
      } else {
         float var6;
         float var7;
         return (double)t < 2.5 / i3
            ? (float)(i2 * (double)(var6 = (float)((double)t - 2.25 / i3)) * (double)var6 + 0.9375)
            : (float)(i2 * (double)(var7 = (float)((double)t - 2.625 / i3)) * (double)var7 + 0.984375);
      }
   }
}
