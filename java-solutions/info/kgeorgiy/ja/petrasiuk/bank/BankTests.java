package info.kgeorgiy.ja.petrasiuk.bank;

import info.kgeorgiy.ja.petrasiuk.bank.test.ClientServerTest;
import info.kgeorgiy.ja.petrasiuk.bank.test.RemoteBankTest;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Provides simple execution for all tests
 */
public class BankTests {
    public static void main(String[] args) {
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(1099);
            LauncherDiscoveryRequest request = org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectClass(ClientServerTest.class),
                            DiscoverySelectors.selectClass(RemoteBankTest.class))
                    .build();

            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener listener = new SummaryGeneratingListener();
            launcher.registerTestExecutionListeners(listener);

            System.out.println("Запуск тестов...");
            launcher.execute(request);

            TestExecutionSummary summary = listener.getSummary();
            System.out.printf("Всего тестов: %d%n", summary.getTestsFoundCount());

            if (summary.getTestsFailedCount() > 0) {
                System.out.println("Проваленные тесты:");
                summary.getFailures().forEach(failure ->
                        System.out.printf("%s: %s%n",
                                failure.getTestIdentifier().getDisplayName(),
                                failure.getException().toString())
                );
            } else {
                System.out.println("Все тесты прошли успешно!");
            }
        } catch (Exception e) {
            System.err.printf("Ошибка при выполнении тестов: %s%n", e.getMessage());
            e.printStackTrace();
        } finally {
            if (registry != null) {
                try {
                    registry.unbind("bank");
                } catch (Exception e) {
                }
            }
        }
    }
}