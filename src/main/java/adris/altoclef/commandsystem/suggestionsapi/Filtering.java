package adris.altoclef.commandsystem.suggestionsapi;

import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;

public class Filtering {
	
	public enum FilteringMode {
		STRICT {
			@Override
			public boolean test(String input, String candidate) {
				return candidate.startsWith(input);
			}
		},
		SLIGHTLY_LOOSE {
			@Override
			public boolean test(String input, String candidate) {
				return candidate.contains(input);
			}
		},
		LOOSE {
			@Override
			public boolean test(String input, String candidate) {
				String[] allWords = input.split("_");
				for (String word : allWords)
					if (!candidate.contains(word))
						return false;

				return true;
			}
		},
		VERY_LOOSE {
			@Override
			public boolean test(String input, String candidate) {
				Char2IntOpenHashMap inputCharCountMap = new Char2IntOpenHashMap();
				for (char c : input.toCharArray())
					inputCharCountMap.addTo(c, 1);

				for (char c : candidate.toCharArray())
					if (inputCharCountMap.containsKey(c)) {
						inputCharCountMap.addTo(c, -1);
						if (inputCharCountMap.get(c) == 0)
							inputCharCountMap.remove(c);
					}

				return inputCharCountMap.isEmpty();
			}
		};

		public boolean test(String input, String candidate) {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
