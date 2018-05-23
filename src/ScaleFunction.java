/*
 * Name: Gabriel Ortega-Gingrich
 * Assignment: Homework 3
 * Description: Implementation of several filters for noise reduction
 */

public interface ScaleFunction<R, M, W, H> {
   R apply(M img, W w, H h);
}
