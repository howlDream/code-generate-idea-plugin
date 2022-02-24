package idea.study.demo;

/**
 *  加载外部类结构
 *      package com;
 *
 *      public class Test {
 *
 *          public void test() {
 *               System.out.println("whistle!!");
 *              System.out.println(this.getClass().getClassLoader());
 *          }
 *
 *       }
 * @author zheng.li
 */
public class MyClassLoaderTestDemo {

    public static void main(String[] args) throws Exception {
        MyClassLoader classLoader = new MyClassLoader();
        Class c = classLoader.loadClass("com.Test");
        c.getMethod("test").invoke(c.newInstance());
    }


    /**
     *
     */

}
