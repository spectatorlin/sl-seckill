package com.seckill;

import java.util.Scanner;

/**
 * @author 邹松林
 * @version 1.0
 * @Title: one
 * @Description: TODO
 * @date 2023/10/11 15:58
 */
public class one {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int[] s = new int[n];
        int[] t = new int[n];
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            s[i]= in.nextInt();
        }
        for (int i = 0; i < n; i++) {
            t[i]= in.nextInt();
        }
        for (int i = 0; i < n; i++) {
            a[i]= in.nextInt();
        }
        int[] dp = new int[t[n - 1]+1000000];
        dp[0]=0;
        int x=0;
        for (int i = 1; i <= s[n - 1]; i++) {
            if (i==s[x]){
                if (x==0){
                    dp[i]=a[x];
                }else {
                    if (i-t[x-1]>0){
                        dp[i]=Math.max(dp[i-t[x-1]]+a[x],dp[i-1]);
                    }else {
                        dp[i]=Math.max(a[x],a[x-1]);
                    }

                }
                x++;
            }else {
                dp[i]=dp[i-1];
            }
        }
        System.out.println(dp[s[n-1]]);
    }
}
