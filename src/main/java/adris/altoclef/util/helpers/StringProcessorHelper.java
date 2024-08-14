package adris.altoclef.util.helpers;

public class StringProcessorHelper {
	public static int findClosestCharIndex(String str, char target, int index) {
        // Edge case: If the string is empty, return -1
        if (str == null || str.length() == 0) {
            return -1;
        }

        // Initialize variables to track the closest indices to the left and right of the given index
        int leftIndex = -1;
        int rightIndex = -1;

        // Search to the left of the given index
        for (int i = index; i >= 0; i--) {
            if (str.charAt(i) == target) {
                leftIndex = i;
                break;
            }
        }

        // Search to the right of the given index
        for (int i = index; i < str.length(); i++) {
            if (str.charAt(i) == target) {
                rightIndex = i;
                break;
            }
        }

        // Determine which of the found indices is closest to the given index
        if (leftIndex == -1 && rightIndex == -1) {
            return -1; // Character not found
        } else if (leftIndex == -1) {
            return rightIndex;
        } else if (rightIndex == -1) {
            return leftIndex;
        } else {
            // Return the closest index
            return (Math.abs(index - leftIndex) <= Math.abs(index - rightIndex)) ? leftIndex : rightIndex;
        }
	}
}
