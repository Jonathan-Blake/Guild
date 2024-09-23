package guild.names;

public abstract class BaseNamedObject implements INamedObject {
    private String name;

    public String getName() {
        if (name == null) {
            name = initName();
        }
        return this.name;
    }

    protected abstract String initName();

    @Override
    public String toString() {
        return super.toString() + " {" +
                "name='" + getName() + '\'' +
                '}';
    }
}
