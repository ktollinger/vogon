<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="org.zlogic.vogon.web.webmessages" />
<div ng-show="authorizationService.authorized">
	<div class="panel panel-default">
		<div class="panel-heading"><fmt:message key="TRANSACTIONS_LIST_TITLE"/></div>
		<div class="panel-body">
			<button ng-click="addTransaction()" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> <fmt:message key="ADD_TRANSACTION"/></button>
			<div infinite-scroll="transactionsService.nextPage()" infinite-scroll-disabled="transactionsService.loadingNextPage">
				<table class="table table-hover">
					<thead>
						<tr>
							<th>
					<div class="editable" ng-click="transactionsService.applySort('description')"><fmt:message key="TRANSACTION_NAME"/>
						<span ng-show="transactionsService.sortColumn === 'description'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="transactionsService.sortAsc?'glyphicon-sort-by-alphabet':'glyphicon - sort - by - alphabet - alt'"></span>
					</div>
					</th>
					<th width="20%">
					<div class="editable" ng-click="transactionsService.applySort('date')"><fmt:message key="DATE"/>
						<span ng-show="transactionsService.sortColumn === 'date'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="transactionsService.sortAsc?'glyphicon-sort-by-order':'glyphicon - sort - by - order - alt'"></span>
					</div>
					</th>
					<th width="20%"><fmt:message key="TAGS"/></th>
					<th class="text-right" width="10%">
					<div class="editable" ng-click="transactionsService.applySort('amount')"><fmt:message key="AMOUNT"/>
						<span ng-show="transactionsService.sortColumn === 'amount'" class="glyphicon glyphicon-sort-by-alphabet" ng-class="transactionsService.sortAsc?'glyphicon-sort-by-order':'glyphicon - sort - by - order - alt'"></span>
					</div>
					</th>
					<th width="10%"><fmt:message key="ACCOUNT"/></th>
					<th width="10%"></th>
					</tr>
					</thead>
					<tbody>
						<tr>
							<td>
								<div class="form-horizontal">
									<div class="input-group">
										<span class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></span>
										<input type="text" class="form-control" placeholder="<fmt:message key="ENTER_DESCRIPTION_FILTER"/>" ng-model="transactionsService.filterDescription" ng-change="applyFilter()"/>
									</div>
								</div>
							</td>
							<td>
								<div class="form-horizontal">
									<div class="input-group">
										<span class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></span>
										<input type="text" class="form-control" datepicker-popup ng-model="transactionsService.filterDate" ng-change="applyFilter()" is-open="filterDateCalendarOpened" placeholder="<fmt:message key="ENTER_DATE_FILTER"/>" />
										<span class="input-group-btn">
											<button type="button" class="btn btn-default" ng-click="openFilterDateCalendar($event)"><span class="glyphicon glyphicon-calendar"></span></button>
										</span>
									</div>
								</div>
							</td>
							<td>
								<div class="form-horizontal">
									<div class="input-group">
										<span class="input-group-addon"><span class="glyphicon glyphicon-filter"></span></span>
										<tags-input class="bootstrap" ng-model="transactionsService.filterTags" placeholder="<fmt:message key="ADD_FILTER_TAGS"/>" on-tag-added="applyFilter()" on-tag-removed="applyFilter()" replace-spaces-with-dashes="false" add-on-comma="false">
											<auto-complete source="tagsService.autocompleteQuery($query)"></auto-complete>
										</tags-input>
									</div>
								</div>
							</td>
							<td></td>
							<td></td>
							<td></td>
						</tr>
						<tr ng-repeat="transaction in transactionsService.transactions" ng-class="{danger:!transactionsService.isAmountOk(transaction)}">
							<td ng-click="startEditing(transaction)" class="editable">{{transaction.description}}</td>
							<td ng-click="startEditing(transaction)" class="editable">{{transaction.date| date}}</td>
							<td ng-click="startEditing(transaction)" class="editable">
								<div ng-repeat="tag in transaction.tags">
									{{tag}}{{$last ? "" : <fmt:message key="TAGS_SEPARATOR" />}}
								</div>
							</td>
							<td ng-click="startEditing(transaction)" class="text-right editable">
								<div ng-repeat="(symbol,total) in totals = (transactionsService.totalsByCurrency(transaction))">
									<span ng-show="transactionsService.isTransferTransaction(transaction)">
										&sum;
									</span>
									{{total| number:2}} {{symbol}}
								</div>
							</td>
							<td ng-click="startEditing(transaction)" class="editable">
								<div ng-show="transactionsService.isExpenseIncomeTransaction(transaction)">
									<div ng-repeat="account in accounts = (transactionsService.getAccounts(transaction, transactionsService.allAccountsPredicate))">
										{{account.name}}{{$last ? '' : ', '}}
									</div>
								</div>
								<div ng-show="transactionsService.isTransferTransaction(transaction)">
									<div ng-repeat="account in accounts = (transactionsService.getAccounts(transaction, transactionsService.fromAccountsPredicate))">
										{{$first && accounts.length > 1 ? '(' : ''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
									</div>
									<span class="glyphicon glyphicon-chevron-down"></span>
									<div ng-repeat="account in accounts = (transactionsService.getAccounts(transaction, transactionsService.toAccountsPredicate))">
										{{$first && accounts.length > 1 ? '(' : ''}}{{account.name}}{{$last ? '' : ', '}}{{$last && accounts.length>1?')':''}}
									</div>
								</div>
							</td>
							<td>
								<button ng-click="duplicateTransaction(transaction)" class="btn btn-default"><span class="glyphicon glyphicon-asterisk"></span> <fmt:message key="DUPLICATE"/></button>
							</td>
						</tr>
						<tr class="text-center" ng-show="transactionsService.loadingNextPage">
							<td colspan="6"><span class="glyphicon glyphicon-refresh"></span> <fmt:message key="LOADING_ALERT"/></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>