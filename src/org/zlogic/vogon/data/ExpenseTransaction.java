/*
 * Vogon personal finance/expense analyzer.
 * License TBD.
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;

/**
 * Implements an expense transaction
 *
 * @author Dmitry Zolotukhin
 */
@Entity
public class ExpenseTransaction extends FinanceTransaction implements Serializable {
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor for an expense transaction
	 */
	public ExpenseTransaction() {
	}

	/**
	 * Constructor for an expense transaction
	 *
	 * @param description The transaction description
	 * @param tags The transaction tags
	 * @param date The transaction date
	 * @param components The expense components
	 */
	public ExpenseTransaction(String description, String[] tags, Date date, List<TransactionComponent> components) {
		this.description = description;
		this.tags = tags;
		this.transactionDate = date;
		this.components = new LinkedList<>();
		this.components.addAll(components);

		updateAmounts();
		
		for (TransactionComponent component : components)
			component.getAccount().updateRawBalance(component.getRawAmount());
	}


	/*
	 * Getters/setters
	 */
	
	/**
	 * Returns the account
	 *
	 * @return the account
	 */
	public FinanceAccount[] getAccounts() {
		HashSet<FinanceAccount> accounts = new HashSet<>();
		for (TransactionComponent component : components) {
			accounts.add(component.getAccount());
		}
		FinanceAccount[] accountsOut = new FinanceAccount[accounts.size()];
		return accounts.toArray(accountsOut);
	}
}
