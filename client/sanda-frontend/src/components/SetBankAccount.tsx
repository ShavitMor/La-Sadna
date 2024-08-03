import React, { useContext, useEffect } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import '../styles/CreateStore.css';
import { addProduct, createNewStore, hasPermission, isOwner, loginUsingJwt, setStoreBankAccount } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import { AppContext } from '../App';
import Permission from '../models/Permission';

// Define the schema for validation
const schema = yup.object().shape({
  bankCode: yup.string().required("Bank Code is required"),
  bankBranchCode: yup.string().required("Bank Branch Code is required"),
  accountCode: yup.string().required("Bank Account code is required"),
  ownerId: yup.string().length(9, "An israeli ID consists of 9 digits").required("Owner ID is required"),
});

const SetBankAccount = () => {
  const { register, handleSubmit, formState: { errors, touchedFields }, trigger } = useForm({
    resolver: yupResolver(schema)
  });

  const {storeId} = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    const checkAllowed = async ()=> {
        let canAccess: boolean = await isOwner(storeId!);
        if(!canAccess){
            navigate('/permission-error', {state: "You do not have permission to set bank account in the given store"})
        }
    }
    checkAllowed();
}, [])

  const onSubmit: SubmitHandler<any> = async (data) => {
    const response = await setStoreBankAccount(parseInt(storeId!), data);
    if (response.error) {
      alert("Error: " + response.errorString);
    } else {
      alert("Bank account changed successfully!");
      navigate(`/store/${storeId}`);
    }
  };

  return (
    <div className="create-store-container">
      <h2>Add New Product</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="create-store-form">
        <div className="form-group">
          <label htmlFor="productName" className='label'>Bank Code</label>
          <input
            id="bankCode"
            placeholder='Enter bank code'
            {...register('bankCode', { onChange: () => trigger("bankCode") })}
            className={errors.bankCode && touchedFields.bankCode ? 'invalid' : ''}
          />
          {errors.bankCode && <p className="error-message">{errors.bankCode.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="bankBranchCode" className='label'>Bank Branch Code</label>
          <input
            id="bankBranchCode"
            placeholder='Enter branch code'
            {...register('bankBranchCode', { onChange: () => trigger("bankBranchCode") })}
            className={errors.bankBranchCode && touchedFields.bankBranchCode ? 'invalid' : ''}
          />
          {errors.bankBranchCode && <p className="error-message">{errors.bankBranchCode.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="accountCode" className='label'>Bank Account Code</label>
          <input
            id="accountCode"
            placeholder='Enter account code'
            {...register('accountCode', { onChange: () => trigger("accountCode") })}
            className={errors.accountCode && touchedFields.accountCode ? 'invalid' : ''}
          />
          {errors.accountCode && <p className="error-message">{errors.accountCode.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="ownerId" className='label'>Account Owner's ID</label>
          <input
            id="ownerId"
            placeholder='Enter ID'
            {...register('ownerId', { onChange: () => trigger("ownerId") })}
            className={errors.ownerId && touchedFields.ownerId ? 'invalid' : ''}
          />
          {errors.ownerId && <p className="error-message">{errors.ownerId.message}</p>}
        </div>
        <button type="submit" className='button'>Change Bank Account</button>
      </form>
    </div>
  );
};

export default SetBankAccount;
