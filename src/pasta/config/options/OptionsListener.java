package pasta.config.options;

import pasta.domain.options.Option;

public interface OptionsListener {
	public boolean actsOn(Option option);
	public void optionUpdated(Option option);
}
