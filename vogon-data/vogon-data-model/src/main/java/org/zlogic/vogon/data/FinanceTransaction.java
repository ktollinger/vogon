/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache 2.0 License: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.Version;

/**
 * Interface for storing a single finance transaction
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Entity
public class FinanceTransaction implements Serializable {

	/**
	 * Localization messages
	 */
	private static final ResourceBundle messages = ResourceBundle.getBundle("org/zlogic/vogon/data/messages");

	/**
	 * The transaction type
	 */
	public enum Type {

		/**
		 * Income or expense
		 */
		EXPENSEINCOME,
		/**
		 * Transfer
		 */
		TRANSFER,
		/**
		 * Unknown (default) value
		 */
		UNDEFINED
	};
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The transaction ID (only for persistence)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	protected Long id;
	/**
	 * JPA version
	 */
	@Version
	private long version = 0L;
	/**
	 * The transaction owner
	 */
	@ManyToOne
	protected VogonUser owner;
	/**
	 * The transaction type
	 */
	protected Type type;
	/**
	 * Contains the expense description string
	 */
	protected String description;
	/**
	 * Contains the expense tags
	 */
	@ElementCollection
	protected Set<String> tags;
	/**
	 * Contains the related accounts and the transaction's distribution into
	 * them
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@OrderBy("id ASC")
	protected Set<TransactionComponent> components;
	/**
	 * Contains the transaction date
	 */
	@Temporal(javax.persistence.TemporalType.DATE)
	protected Date transactionDate;
	/**
	 * The transaction amount
	 */
	protected long amount;

	/**
	 * Default constructor
	 */
	public FinanceTransaction() {
		type = Type.UNDEFINED;
		amount = 0;
	}

	/**
	 * Creates a new FinanceTransaction
	 *
	 * @param owner the transaction owner
	 * @param description the transaction description
	 * @param tags the transaction tags
	 * @param date the transaction date
	 * @param type the transaction type
	 */
	public FinanceTransaction(VogonUser owner, String description, String[] tags, Date date, Type type) {
		this();
		this.description = description;
		this.tags = tags != null ? new HashSet<>(Arrays.asList(tags)) : new HashSet<String>();
		this.transactionDate = date;
		this.components = new HashSet<>();
		this.type = type;
		FinanceTransaction.this.setOwner(owner);
	}

	/**
	 * Creates a new FinanceTransaction by merging another transaction's
	 * properties
	 *
	 * @param owner the transaction owner
	 * @param transaction the transaction from which to merge properties
	 */
	public FinanceTransaction(VogonUser owner, FinanceTransaction transaction) {
		this.components = new HashSet<>();
		FinanceTransaction.this.merge(transaction);
		FinanceTransaction.this.setOwner(owner);
	}

	/**
	 * Merges properties from another FinanceTransaction instance to this
	 * instance; only merges properties, and not components
	 *
	 * @param transaction the transaction from which to merge properties
	 */
	public void merge(FinanceTransaction transaction) {
		if (version != transaction.version)
			throw new ConcurrentModificationException(messages.getString("TRANSACTION_WAS_ALREADY_UPDATED"));
		this.type = transaction.type;
		this.description = transaction.description;
		this.tags = new HashSet<>();
		for (String tag : transaction.tags)
			this.tags.add(tag);
		this.transactionDate = (Date) transaction.transactionDate.clone();
	}

	@Override
	public FinanceTransaction clone() {
		FinanceTransaction cloneTransaction = new FinanceTransaction(this.owner, this);
		for (TransactionComponent component : components)
			cloneTransaction.addComponent(new TransactionComponent(component.getAccount(), cloneTransaction, component.getRawAmount()));
		cloneTransaction.transactionDate = new Date();
		return cloneTransaction;
	}

	/**
	 * Adds component to this account
	 *
	 * @param component the component to add
	 */
	public void addComponent(TransactionComponent component) {
		this.components.add(component);
		updateAmounts();

		if (component.getAccount() != null)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/**
	 * Adds components to this account
	 *
	 * @param components the components to add
	 */
	public void addComponents(List<TransactionComponent> components) {
		this.components.addAll(components);
		updateAmounts();

		for (TransactionComponent component : components)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/**
	 * Removes a transaction component
	 *
	 * @param component the component to be removed
	 */
	public void removeComponent(TransactionComponent component) {
		if (!components.contains(component))
			return;
		if (component.getAccount() != null)
			component.getAccount().updateRawBalance(-component.getRawAmount());
		component.setAccount(null);
		component.setTransaction(null);
		components.remove(component);
		updateAmounts();
	}

	/**
	 * Removes all transaction components
	 *
	 */
	public void removeAllComponents() {
		for (TransactionComponent component : components) {
			if (component.getAccount() != null)
				component.getAccount().updateRawBalance(-component.getRawAmount());
			component.setAccount(null);
			component.setTransaction(null);
		}
		components.clear();
		updateAmounts();
	}

	/**
	 * Updates the transaction's amount from its components
	 */
	public void updateAmounts() {
		if (components == null)
			return;
		if (type == Type.EXPENSEINCOME) {
			amount = 0;
			for (TransactionComponent component : components)
				amount += component.getRawAmount();
		} else if (type == Type.TRANSFER) {
			long amountPositive = 0, amountNegative = 0;
			for (TransactionComponent component : components) {
				amountPositive += component.getRawAmount() > 0 ? component.getRawAmount() : 0;
				amountNegative += component.getRawAmount() < 0 ? component.getRawAmount() : 0;
			}
			amount = amountPositive > -amountNegative ? amountPositive : -amountNegative;
		}
	}

	/**
	 * Returns a list of all components associated with an account
	 *
	 * @param account the account to search
	 * @return the list of transaction components associated with the searched
	 * account
	 */
	public List<TransactionComponent> getComponentsForAccount(FinanceAccount account) {
		List<TransactionComponent> foundComponents = new LinkedList<>();
		for (TransactionComponent component : components)
			if (component.getAccount().equals(account))
				foundComponents.add(component);
		return foundComponents;
	}

	/**
	 * Returns a list of all accounts affected by this transaction
	 *
	 * @return the list of all accounts affected by this transaction
	 */
	public List<FinanceAccount> getAccounts() {
		List<FinanceAccount> accounts = new LinkedList<>();
		for (TransactionComponent component : components)
			if (!accounts.contains(component.getAccount()))
				accounts.add(component.getAccount());
		return accounts;
	}

	/**
	 * Returns a list of all components
	 *
	 * @return the list of all transaction components
	 */
	public List<TransactionComponent> getComponents() {
		List<TransactionComponent> foundComponents = new LinkedList<>();
		foundComponents.addAll(components);
		return foundComponents;
	}

	/**
	 * Sets a new amount for a component and updates the transaction and account
	 * balance
	 *
	 * @param component the component to be updated
	 * @param amount the new amount
	 */
	public void updateComponentRawAmount(TransactionComponent component, double amount) {
		updateComponentRawAmount(component, Math.round(amount * Constants.RAW_AMOUNT_MULTIPLIER));
	}

	/**
	 * Sets a new amount for a component and updates the transaction and account
	 * balance
	 *
	 * @param component the component to be updated
	 * @param amount the new amount
	 */
	public void updateComponentRawAmount(TransactionComponent component, long amount) {
		if (!components.contains(component))
			return;
		long deltaAmount = amount - component.getRawAmount();
		component.setRawAmount(amount);
		updateAmounts();
		if (component.getAccount() != null)
			component.getAccount().updateRawBalance(deltaAmount);
	}

	/**
	 * Sets a new account for a component and updates the account balance
	 *
	 * @param component the component to be updated
	 * @param account the new account
	 */
	public void updateComponentAccount(TransactionComponent component, FinanceAccount account) {
		if (!components.contains(component))
			return;
		if (component.getAccount() != null)
			component.getAccount().updateRawBalance(-component.getRawAmount());
		component.setAccount(account);
		if (component.getAccount() != null)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}

	/**
	 * Delete the listed components
	 *
	 * @param components the components to delete
	 */
	public void removeComponents(List<TransactionComponent> components) {
		this.components.removeAll(components);
		updateAmounts();
	}

	/**
	 * Returns if the amount is OK (e.g. for transfer transactions sum is zero
	 * or accounts use different currencies)
	 *
	 * @return true if amount is OK
	 */
	public boolean isAmountOk() {
		if (type == Type.EXPENSEINCOME)
			return true;
		else if (type == Type.TRANSFER) {
			long sum = 0;
			Currency commonCurrency = null;
			for (TransactionComponent component : components) {
				if (commonCurrency == null && component.getAccount() != null)
					commonCurrency = component.getAccount().getCurrency();
				else if (component.getAccount() != null && component.getAccount().getCurrency() != commonCurrency)
					return true;
				sum += component.getRawAmount();
			}
			return sum == 0;
		} else if (type == Type.UNDEFINED)
			return false;
		else
			return true;
	}

	/*
	 * Getters/setters
	 */
	/**
	 * Returns the raw amount (should be divided by
	 * Constants.rawAmountMultiplier to get the real amount)
	 *
	 * @return the transaction amount
	 */
	public long getRawAmount() {
		return amount;
	}

	/**
	 * Returns the transaction amount
	 *
	 * @return the transaction amount
	 */
	public double getAmount() {
		return amount / Constants.RAW_AMOUNT_MULTIPLIER;
	}

	/**
	 * Returns a list of all currencies used in this transaction's components
	 *
	 * @return the list of all currencies used in this transaction's components
	 */
	public List<Currency> getCurrencies() {
		List<Currency> currencies = new LinkedList<>();
		for (TransactionComponent component : components)
			if (component.getAccount() != null && !currencies.contains(component.getAccount().getCurrency()))
				currencies.add(component.getAccount().getCurrency());
		return currencies;
	}

	/**
	 * Adds a tag
	 *
	 * @param tag the tag to add
	 */
	void addTag(String tag) {
		if (tags == null)
			tags = new HashSet<>();
		if (!tags.contains(tag))
			tags.add(tag);
	}

	/**
	 * For transfer transactions returns the account accountFrom which money was
	 * transferred. For expense/income transactions: returns empty array
	 *
	 * @return the source accounts
	 */
	public FinanceAccount[] getFromAccounts() {
		if (type == Type.TRANSFER) {
			HashSet<FinanceAccount> accounts = new HashSet<>();
			for (TransactionComponent component : components)
				if (component.getRawAmount() < 0)
					accounts.add(component.getAccount());
			return accounts.toArray(new FinanceAccount[0]);
		} else
			return new FinanceAccount[0];
	}

	/**
	 * For transfer transactions returns the accounts account to which money was
	 * transferred, For expense/income transactions: returns empty array
	 *
	 * @return the destination accounts
	 */
	public FinanceAccount[] getToAccounts() {
		if (type == Type.TRANSFER) {
			HashSet<FinanceAccount> accounts = new HashSet<>();
			for (TransactionComponent component : components)
				if (component.getRawAmount() > 0)
					accounts.add(component.getAccount());
			return accounts.toArray(new FinanceAccount[0]);
		} else
			return new FinanceAccount[0];
	}

	/**
	 * Returns the transaction's description
	 *
	 * @return the transaction's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the transaction's description
	 *
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the transaction's tags
	 *
	 * @return the transaction's tags
	 */
	public String[] getTags() {
		return tags.toArray(new String[0]);
	}

	/**
	 * Sets the transaction's tags
	 *
	 * @param tags the new transaction's tags
	 */
	public void setTags(String... tags) {
		this.tags = new HashSet<>(Arrays.asList(tags));
	}

	/**
	 * Returns the transaction date
	 *
	 * @return the transaction date
	 */
	public Date getDate() {
		return transactionDate;
	}

	/**
	 * Sets the transaction date
	 *
	 * @param date the transaction date
	 */
	public void setDate(Date date) {
		this.transactionDate = date;
	}

	/**
	 * Returns the transaction type
	 *
	 * @return the transaction type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets the transaction type
	 *
	 * @param type the transaction type to set
	 */
	public void setType(Type type) {
		this.type = type;
		updateAmounts();
	}

	/**
	 * Returns the transaction owner
	 *
	 * @return the transaction owner
	 */
	public VogonUser getOwner() {
		return owner;
	}

	/**
	 * Sets the transaction owner
	 *
	 * @param owner the owner to set
	 */
	public void setOwner(VogonUser owner) {
		this.owner = owner;
	}

	/**
	 * Returns the ID for this class instance
	 *
	 * @return the ID for this class instance
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Returns the version for this class instance
	 *
	 * @return the version for this class instance
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Sets the version of this class instance
	 *
	 * @param version the version of this class instance
	 */
	protected void setVersion(long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FinanceTransaction)
			return id != null ? id.equals(((FinanceTransaction) obj).id) : false;
		else
			return this == obj;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + (int) (this.id ^ (this.id >>> 32));
		return hash;
	}
}
