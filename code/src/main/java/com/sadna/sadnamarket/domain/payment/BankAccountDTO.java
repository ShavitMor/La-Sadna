package com.sadna.sadnamarket.domain.payment;

import com.sadna.sadnamarket.domain.stores.Store;
import com.sadna.sadnamarket.domain.stores.StoreDTO;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "bank_accounts")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BankAccountDTO {
/*
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id")
    private Integer accountId;
*/
    @Id
    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_branch_code")
    private String bankBranchCode;

    @Column(name = "account_code")
    private String accountCode;

    @Column(name = "owner_username")
    private String ownerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public BankAccountDTO(String bankCode, String bankBranchCode, String accountCode, String ownerId) {
        this.bankCode = bankCode;
        this.bankBranchCode = bankBranchCode;
        this.accountCode = accountCode;
        this.ownerId = ownerId;
        this.store = null;
    }

    public BankAccountDTO() {}

    public String getBankCode() {
        return bankCode;
    }

    public String getBankBranchCode() {
        return bankBranchCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccountDTO that = (BankAccountDTO) o;
        return Objects.equals(bankCode, that.bankCode) && Objects.equals(bankBranchCode, that.bankBranchCode) && Objects.equals(accountCode, that.accountCode) && Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankCode, bankBranchCode, accountCode, ownerId);
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.storeId = store.getStoreId();
        this.store = store;
    }
}
