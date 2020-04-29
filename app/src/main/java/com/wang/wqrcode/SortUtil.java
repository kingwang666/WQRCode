package com.wang.wqrcode;

import androidx.annotation.Nullable;

import java.util.Random;

/**
 * 各种无聊的排序算法
 * Author: wangxiaojie6
 * Date: 2018/6/25
 */
public class SortUtil {

    public static class Int {

        @Nullable
        public static int[] getRandomArray(int length) {
            if (length > 0) {
                Random random = new Random();
                int[] array = new int[length];
                for (int i = 0; i < length; i++) {
                    array[i] = random.nextInt(66);
                }
                return array;
            }
            return null;
        }

        public static void bubble(@Nullable int[] array) {
            bubble(array, false);
        }

        public static void bubble(@Nullable int[] array, boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                int temp;
                boolean flag;
                for (int i = 0; i < length - 1; i++) {
                    flag = false;
                    for (int j = length - 1; j > i; j--) {
                        if ((!reverse && array[j] < array[j - 1]) || (reverse && array[j] > array[j - 1])) {
                            temp = array[j];
                            array[j] = array[j - 1];
                            array[j - 1] = temp;
                            flag = true;
                        }
                    }
                    if (!flag) {
                        break;
                    }
                }
            }
        }

        public static void select(@Nullable int[] array) {
            select(array, false);
        }

        public static void select(@Nullable int[] array, boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                int minIndex;
                int temp;
                for (int i = 0; i < length - 1; i++) {
                    minIndex = i;
                    for (int j = i + 1; j < length; j++) {
                        if ((!reverse && array[j] < array[minIndex]) || (reverse && array[j] > array[minIndex])) {
                            minIndex = j;
                        }
                    }
                    if (minIndex != i) {
                        temp = array[i];
                        array[i] = array[minIndex];
                        array[minIndex] = temp;
                    }
                }
            }
        }

        public static void insert(@Nullable int[] array) {
            insert(array, false);
        }

        public static void insert(@Nullable int[] array, boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                int temp;
                for (int i = 0; i < length - 1; i++) {
                    for (int j = i + 1; j > 0; j--) {
                        if ((!reverse && array[j] < array[j - 1]) || (reverse && array[j] > array[j - 1])) {
                            temp = array[j - 1];
                            array[j - 1] = array[j];
                            array[j] = temp;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        public static void shell(int array[]) {
            shell(array, false);
        }

        public static void shell(int array[], boolean reverse) {

            int length;
            if (array != null && (length = array.length) > 1) {
                int temp;
                int incre = length;

                while (true) {
                    incre = incre / 2;

                    for (int k = 0; k < incre; k++) {

                        for (int i = k + incre; i < length; i += incre) {

                            for (int j = i; j > k; j -= incre) {
                                if ((!reverse && array[j] < array[j - incre]) || (reverse && array[j] > array[j - incre])) {
                                    temp = array[j - incre];
                                    array[j - incre] = array[j];
                                    array[j] = temp;
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    if (incre == 1) {
                        break;
                    }
                }
            }
        }

        public static void quick(int array[]) {
            quick(array, false);
        }

        public static void quick(int array[], boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                quick(array, 0, length - 1, (length - 1) / 2, reverse);
            }
        }


        private static void quick(int array[], int left, int right, int keyIndex, boolean reverse) {
            if (left >= right) {
                return;
            }

            int i = left;
            int j = right;
            int key = array[keyIndex];

            while (i <= j) {

                while (i <= j && keyIndex < j && ((!reverse && array[j] > key) || (reverse && array[j] < key))) {
                    j--;
                }

                if (i <= j && keyIndex != j) {
                    array[keyIndex] = array[j];
                    if (keyIndex == i) {
                        i++;
                    }
                    keyIndex = j;
                    j--;
                }

                while (i <= j && i < keyIndex && ((!reverse && array[i] < key) || (reverse && array[i] > key))) {
                    i++;
                }
                if (i <= j && keyIndex != i) {
                    array[keyIndex] = array[i];
                    keyIndex = i;
                    i++;
                }
                if (i == j && array[i] == key) {
                    break;
                }
            }
            array[keyIndex] = key;
            quick(array, left, keyIndex - 1, (left + keyIndex - 1) / 2, reverse);//递归调用
            quick(array, keyIndex + 1, right, (keyIndex + 1 + right) / 2, reverse);//递归调用
        }

        public static void merge(int array[]) {
            merge(array, false);
        }


        public static void merge(int array[], boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                int[] temp = new int[length];
                merge(array, 0, length - 1, temp, reverse);
            }
        }

        private static void merge(int array[], int first, int last, int temp[], boolean reverse) {

            if (first < last) {
                int middle = (first + last) / 2;
                merge(array, first, middle, temp, reverse);//左半部分排好序
                merge(array, middle + 1, last, temp, reverse);//右半部分排好序
                mergeArray(array, first, middle, last, temp, reverse); //合并左右部分
            }
        }


        //合并 ：将两个序列a[first-middle],a[middle+1-end]合并
        private static void mergeArray(int array[], int first, int middle, int end, int temp[], boolean reverse) {
            int i = first;
            int j = middle + 1;
            int k = 0;
            while (i <= middle && j <= end) {
                if ((!reverse && array[i] < array[j]) || (reverse && array[i] > array[j])) {
                    temp[k] = array[i];
                    k++;
                    i++;
                } else {
                    temp[k] = array[j];
                    k++;
                    j++;
                }
            }
            while (i <= middle) {
                temp[k] = array[i];
                k++;
                i++;
            }
            while (j <= end) {
                temp[k] = array[j];
                k++;
                j++;
            }

            System.arraycopy(temp, 0, array, first, k);
        }

        public static void heap(int array[]) {
            heap(array, false);
        }

        public static void heap(int array[], boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                int temp;
                makeHeap(array, length, reverse);

                for (int i = length - 1; i > 0; i--) {
                    temp = array[0];
                    array[0] = array[i];
                    array[i] = temp;
                    heapFixdown(array, 0, i, reverse);
                }
            }
        }

        //构建最小堆
        private static void makeHeap(int array[], int n, boolean min) {
            for (int i = (n - 1) / 2; i >= 0; i--) {
                heapFixdown(array, i, n, min);
            }
        }

        //从i节点开始调整,n为节点总数 从0开始计算 i节点的子节点为 2*i+1, 2*i+2
        private static void heapFixdown(int array[], int i, int n, boolean min) {

            int j = 2 * i + 1; //子节点
            int temp;

            while (j < n) {
                //在左右子节点中寻找
                if (j + 1 < n && ((min && array[j + 1] < array[j]) || (!min && array[j + 1] > array[j]))) {
                    j++;
                }

                if ((min && array[i] <= array[j]) || (!min && array[i] >= array[j]))
                    break;

                //节点下移
                temp = array[i];
                array[i] = array[j];
                array[j] = temp;

                i = j;
                j = 2 * i + 1;
            }
        }


        public static void radix(int array[]) {
            radix(array, -1, false);
        }

        public static void radix(int array[], int k) {
            radix(array, k, false);
        }

        /**
         * 基数排序 不适用于小数排序
         *
         * @param array 排序数组
         */
        public static void radix(int array[], int k, boolean reverse) {
            int length;
            if (array != null && (length = array.length) > 1) {
                int radix = 10;
                int residue;
                int[] bin = new int[radix];
                int[] temp = new int[length];

                if (k == -1) {
                    int max = array[0];
                    for (int i = 1; i < length; i++) {
                        max = Math.max(max, array[i]);
                    }
                    k = 1;
                    while ((max /= 10) > 0) {
                        k++;
                    }
                }

                for (int i = 0, r = 1; i < k; i++, r = r * radix) {
                    //初始化
                    for (int j = 0; j < radix; j++) {
                        bin[j] = 0;
                    }
                    //计算每个箱子的数字个数
                    for (int j = 0; j < length; j++) {
                        residue = (array[j] / r) % radix;
                        bin[residue]++;
                    }
                    //cnt[j]的个数修改为前j个箱子一共有几个数字
                    for (int j = 1; j < radix; j++) {
                        bin[j] = bin[j - 1] + bin[j];
                    }
                    for (int j = length - 1; j >= 0; j--) {
                        residue = (array[j] / r) % radix;
                        temp[--bin[residue]] = array[j];
                    }
                    if (reverse && i == k - 1) {
                        for (int j = 0; j < length; j++) {
                            array[length - j - 1] = temp[j];
                        }
                    } else {
                        System.arraycopy(temp, 0, array, 0, length);
                    }
                }
            }
        }

    }

}
