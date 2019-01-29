package no.nav.vedtak.felles.integrasjon.jms;

import java.util.Objects;

import no.nav.vedtak.feil.FeilFactory;

/**
 * Gir konfigurasjonsverdier felles for alle meldingskøer brukt i VL.
 * I praksis går disse verdiene på forbindelsen til selve MQ-serveren som VL bruker.
 * </p>
 * <p>
 * De enkelte meldingskøene har sin konkrete sub-klasse, med konfigurasjonsverdier for selve køen.
 */
public class BaseJmsKonfig implements JmsKonfig {

    public static final String JNDI_JMS_CONNECTION_FACTORY = "jms/ConnectionFactory";

    private static final String MQ_GATEWAY_PREFIX = "mqGateway02"; // queue manager
    private static final String NAME = ".name"; // NOSONAR //$NON-NLS-1$

    private String queueKeyPrefix;

    public BaseJmsKonfig(String queueKeyPrefix) {
        Objects.requireNonNull(queueKeyPrefix, "queueKeyPrefix"); //$NON-NLS-1$

        this.queueKeyPrefix = queueKeyPrefix;
    }

    protected String getProperty(String key) {
        String val = System.getProperty(key);
        if (val == null || val.isEmpty()) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
            if (val == null || val.isEmpty()) {
                throw FeilFactory.create(JmsFeil.class).manglerNødvendigSystemProperty(key).toException();
            }
        }
        return val;
    }

    protected int getPropertyInt(String key) {
        String value = getProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw FeilFactory.create(JmsFeil.class).ikkeIntegerSystemProperty(key).toException();
        }
    }

    protected void setProperty(String key, String value) {
        System.setProperty(key, value);
    }

    @Override
    public String getQueueManagerChannelName() {
        return getProperty(getQueueManagerChannelNamePropertyKey());
    }

    public String getQueueManagerChannelNamePropertyKey() {
        return MQ_GATEWAY_PREFIX + ".channel"; // $NON-NLS-1$
    }

    @Override
    public String getQueueManagerHostname() {
        return getProperty(getQueueManagerHostnamePropertyKey());
    }

    public String getQueueManagerHostnamePropertyKey() {
        return MQ_GATEWAY_PREFIX + ".hostname"; //$NON-NLS-1$
    }

    @Override
    public String getQueueManagerName() {
        return getProperty(getQueueManagerNamePropertyKey());
    }

    public String getQueueManagerNamePropertyKey() {
        return MQ_GATEWAY_PREFIX + NAME; // $NON-NLS-1$
    }

    @Override
    public int getQueueManagerPort() {
        return getPropertyInt(getQueueManagerPortPropertyKey());
    }

    public String getQueueManagerPortPropertyKey() {
        return MQ_GATEWAY_PREFIX + ".port"; //$NON-NLS-1$
    }

    @Override
    public String getQueueManagerUsername() {
        // return getProperty(getQueueManagerUsernamePropertyKey()); //NOSONAR
        // TODO (rune) ta fra ekstern kilde når det nye sikkehetsregimet er på plass

        return "srvappserver";
    }

    public String getQueueManagerUsernamePropertyKey() {
        return MQ_GATEWAY_PREFIX + ".username"; //$NON-NLS-1$
    }

    public static String getQueueManagerPropertyPrefix() {
        return MQ_GATEWAY_PREFIX;
    }

    @Override
    public String getQueueName() {
        return getProperty(getQueueNamePropertyKey());
    }

    public String getQueueNamePropertyKey() {
        return queueKeyPrefix + ".queueName"; //$NON-NLS-1$
    }

    public void setQueueManagerChannelName(String value) {
        setProperty(getQueueManagerChannelNamePropertyKey(), value);
    }

    public void setQueueManagerHostname(String value) {
        setProperty(getQueueManagerHostnamePropertyKey(), value);
    }

    public void setQueueManagerName(String value) {
        setProperty(getQueueManagerNamePropertyKey(), value);
    }

    public void setQueueManagerPort(int value) {
        setProperty(getQueueManagerPortPropertyKey(), Integer.toString(value));
    }

    public void setQueueManagerUsername(String value) {
        setProperty(getQueueManagerUsernamePropertyKey(), value);
    }

    public void setQueueName(String value) {
        setProperty(getQueueNamePropertyKey(), value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
                + ", queue=" + getQueueManagerName()
                + ",channel=" + getQueueManagerChannelName()
                + ", host=" + getQueueManagerHostname()
                + ", port" + getQueueManagerPort()
                + ", username=" + getQueueManagerUsername()
                + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        BaseJmsKonfig other = (BaseJmsKonfig) obj;
        return Objects.equals(getQueueName(), other.getQueueName())
                && Objects.equals(getQueueManagerChannelName(), other.getQueueManagerChannelName())
                && Objects.equals(getQueueManagerHostname(), other.getQueueManagerHostname())
                && Objects.equals(getQueueManagerPort(), other.getQueueManagerPort())
                && Objects.equals(getQueueManagerUsername(), other.getQueueManagerUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQueueManagerChannelName(), getQueueManagerHostname(), getQueueManagerPort(), getQueueManagerUsername(),
                getQueueName());
    }

}