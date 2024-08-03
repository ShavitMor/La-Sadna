import React, { useContext, useEffect, useState } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import '../styles/CreateStore.css';
import { addProduct, buyCart, createNewStore, hasPermission, isOwner, loginUsingJwt, setStoreBankAccount } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import { AppContext } from '../App';
import Permission from '../models/Permission';
import { CreditCard } from '@mui/icons-material';
import { Loading } from './Loading';

// Define the schema for validation
const schema = yup.object().shape({
  creditCard: yup.string().length(16, "A credit card number is 16 digits long").required("Credit Card is required"),
  digitsOnBack: yup.string().length(3, "All 3 digits are required").required("3 Digits on the back are required"),
  expirationDate: yup.date().min(new Date(), "date cannot be in the past").required("Expiration date is required"),
  ownerId: yup.string().length(9, "An israeli ID consists of 9 digits").required("Owner ID is required"),
  country: yup.string().required("Country is required"),
  city: yup.string().required("City is required"),
  addressLine1: yup.string().required("Address Line 1 is required"),
  addressLine2: yup.string().required("Address Line 2 is required"),
  zipCode: yup.string().required("Zip Code is required"),
  ordererName: yup.string().required("Orderer Name is required"),
  contactPhone: yup.string().required("Contact Phone is required"),
  contactEmail: yup.string().email("Contact Email must be a valid email").required("Contact Email is required"),
});

const Purchase = () => {
  const { register, handleSubmit, formState: { errors, touchedFields }, trigger } = useForm({
    resolver: yupResolver(schema)
  });
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onSubmit: SubmitHandler<any> = async (data) => {
    setLoading(true);
    const response = await buyCart(data);
    if (response.error) {
      alert("Error: " + response.errorString);
    } else {
      alert("Purchase is successful!");
      navigate(`/`);
    }
    setLoading(false);
  };

  return (
    <div className="create-store-container">
      <h2>Add New Product</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="create-store-form">
      <div className="form-group">
  <label htmlFor="creditCard" className='label'>Credit Card</label>
  <input
    id="creditCard"
    placeholder='Enter credit card'
    {...register('creditCard', { onChange: () => trigger("creditCard") })}
    className={errors.creditCard && touchedFields.creditCard ? 'invalid' : ''}
  />
  {errors.creditCard && <p className="error-message">{errors.creditCard.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="digitsOnBack" className='label'>3 Digits on the back</label>
  <input
    id="digitsOnBack"
    placeholder='Enter 3 digits on back'
    {...register('digitsOnBack', { onChange: () => trigger("digitsOnBack") })}
    className={errors.digitsOnBack && touchedFields.digitsOnBack ? 'invalid' : ''}
  />
  {errors.digitsOnBack && <p className="error-message">{errors.digitsOnBack.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="expirationDate" className='label'>Expiration Date</label>
  <input
    id="expirationDate"
    type='date'
    placeholder='Enter expiration date'
    {...register('expirationDate', { onChange: () => trigger("expirationDate") })}
    className={errors.expirationDate && touchedFields.expirationDate ? 'invalid' : ''}
  />
  {errors.expirationDate && <p className="error-message">{errors.expirationDate.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="ownerId" className='label'>Owner ID</label>
  <input
    id="ownerId"
    placeholder='Enter owner ID'
    {...register('ownerId', { onChange: () => trigger("ownerId") })}
    className={errors.ownerId && touchedFields.ownerId ? 'invalid' : ''}
  />
  {errors.ownerId && <p className="error-message">{errors.ownerId.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="country" className='label'>Country</label>
  <input
    id="country"
    placeholder='Enter country'
    {...register('country', { onChange: () => trigger("country") })}
    className={errors.country && touchedFields.country ? 'invalid' : ''}
  />
  {errors.country && <p className="error-message">{errors.country.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="city" className='label'>City</label>
  <input
    id="city"
    placeholder='Enter city'
    {...register('city', { onChange: () => trigger("city") })}
    className={errors.city && touchedFields.city ? 'invalid' : ''}
  />
  {errors.city && <p className="error-message">{errors.city.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="addressLine1" className='label'>Address Line 1</label>
  <input
    id="addressLine1"
    placeholder='Enter address line 1'
    {...register('addressLine1', { onChange: () => trigger("addressLine1") })}
    className={errors.addressLine1 && touchedFields.addressLine1 ? 'invalid' : ''}
  />
  {errors.addressLine1 && <p className="error-message">{errors.addressLine1.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="addressLine2" className='label'>Address Line 2</label>
  <input
    id="addressLine2"
    placeholder='Enter address line 2'
    {...register('addressLine2', { onChange: () => trigger("addressLine2") })}
    className={errors.addressLine2 && touchedFields.addressLine2 ? 'invalid' : ''}
  />
  {errors.addressLine2 && <p className="error-message">{errors.addressLine2.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="zipCode" className='label'>Zip Code</label>
  <input
    id="zipCode"
    placeholder='Enter zip code'
    {...register('zipCode', { onChange: () => trigger("zipCode") })}
    className={errors.zipCode && touchedFields.zipCode ? 'invalid' : ''}
  />
  {errors.zipCode && <p className="error-message">{errors.zipCode.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="ordererName" className='label'>Orderer Name</label>
  <input
    id="ordererName"
    placeholder='Enter orderer name'
    {...register('ordererName', { onChange: () => trigger("ordererName") })}
    className={errors.ordererName && touchedFields.ordererName ? 'invalid' : ''}
  />
  {errors.ordererName && <p className="error-message">{errors.ordererName.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="contactPhone" className='label'>Contact Phone</label>
  <input
    id="contactPhone"
    placeholder='Enter contact phone'
    {...register('contactPhone', { onChange: () => trigger("contactPhone") })}
    className={errors.contactPhone && touchedFields.contactPhone ? 'invalid' : ''}
  />
  {errors.contactPhone && <p className="error-message">{errors.contactPhone.message}</p>}
</div>

<div className="form-group">
  <label htmlFor="contactEmail" className='label'>Contact Email</label>
  <input
    id="contactEmail"
    placeholder='Enter contact email'
    {...register('contactEmail', { onChange: () => trigger("contactEmail") })}
    className={errors.contactEmail && touchedFields.contactEmail ? 'invalid' : ''}
  />
  {errors.contactEmail && <p className="error-message">{errors.contactEmail.message}</p>}
</div>
        <button type="submit" className='button'>Complete Purchase</button>
        {loading && <Loading reason={"your purchase is being finalized"}/>}
      </form>
    </div>
  );
};

export default Purchase;
