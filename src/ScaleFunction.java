public interface ScaleFunction<R, M, W, H> {
   R apply(M img, W w, H h);
}
