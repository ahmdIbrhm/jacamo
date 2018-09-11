package jacamo.platform;

import jacamo.util.Config;

public class OrganisationWebInspector extends DefaultPlatform {

    @Override
    public void init(String[] args) {
        if (args.length == 1) {
            Config.get().setProperty(Config.START_WEB_OI, args[0]);
        }
    }
}
