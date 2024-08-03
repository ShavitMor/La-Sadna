import React, { useContext, useEffect, useState } from 'react';
import '../styles/profile.css';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { getMember, loginUsingJwt, updateBirthday, updateEmail, updateFirstName, updateLastName, updatePhone } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import MemberModel from '../models/MemberModel';
import { formatDate } from '../utils';
import { AppContext } from '../App';


const schema = yup.object().shape({
  username: yup.string().required("Username is required"),
  firstName: yup.string().required("First Name is required"),
  lastName: yup.string().required("Last Name is required"),
  emailAddress: yup.string().email("Invalid email format").required("Email is required"),
  phoneNumber: yup.string().required("Phone number is required"),
  birthDate: yup.string().max(new Date().getDate()).required("Birthday is required")
});

const Profile = () => {
  const {username} = useParams();
  const [defaultValues, setDefaultValues] = useState({username: '', firstName: '', lastName: '', emailAddress: '', phoneNumber: '', birthDate: ''});  
  const {isloggedin , setIsloggedin } = useContext(AppContext);
  const navigate = useNavigate();
  useEffect(() => {
    const fetchProfile = async () => {
      if(!isloggedin){
        const resp=await loginUsingJwt(localStorage.getItem("username") as string, localStorage.getItem("token") as string);
            if(!resp.error){
             setIsloggedin(true);
            }
            else{
              localStorage.clear();
              alert("Session over please login again");
              navigate('/');
            }
      }
      const profileData = await getMember(username as string);
      setDefaultValues(profileData as MemberModel);
    };
    try{
    fetchProfile();
    }catch{
      alert("Error occoured please try again later");
      navigate('/');
    }
  }, []);

  const {
    register,
    handleSubmit,
    formState: { errors, touchedFields },
    reset,
    trigger
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: defaultValues
  });
  useEffect(() => {
    if (defaultValues) {
      reset(defaultValues); // Reset the form with fetched data
    }
  }, [defaultValues, reset]);

  const onSubmit = async (data: any) => {
    console.log('Profile Saved', data);
    try{
    if(data.firstName !== defaultValues.firstName) {
      const response=await updateFirstName(data.firstName);
      if(response.error)
        alert(response.error);
    }
    if(data.lastName !== defaultValues.lastName) {
      const response=await updateLastName(data.lastName);
      if(response.error)
        alert(response.error);
    }
    if(data.emailAddress !== defaultValues.emailAddress) {
      const response=await updateEmail(data.emailAddress);
      if(response.error)
        alert(response.error);
    }
    if(data.phoneNumber !== defaultValues.phoneNumber) {
      const response=await updatePhone(data.phoneNumber);
      if(response.error)
        alert(response.error);
    }
    if(data.birthDate!== defaultValues.birthDate) {
      data.birthDate = formatDate(new Date(data.birthDate).toISOString());
      const birthDate = new Date(data.birthDate);
      const currentDate = new Date();

      if (birthDate > currentDate) {
        alert("birthday cannot be in the future");
        return;
      }
      const response=await updateBirthday(data.birthDate);
      if(response.error)
        alert(response.error);
    }
    setDefaultValues(data);
  }catch(e){
    alert("Error occoured please try again later");
  };
};

  const handleReset = () => {
    reset();
  };

  return (
    <div className="profile-page">
      <h1>Profile</h1>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="form-group">
          <label htmlFor="username">Username:</label>
          <input
            type="text"
            id="username"
            {...register("username", { onChange: () => trigger("username") })}
            className={errors.username && touchedFields.username ? 'invalid' : ''}
            disabled={true}
          />
          {errors.username && <p className="error-message">{errors.username.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="firstName">First Name:</label>
          <input
            type="text"
            id="firstName"
            {...register("firstName", { onChange: () => trigger("firstName") })}
            className={errors.firstName && touchedFields.firstName ? 'invalid' : ''}
          />
          {errors.firstName && <p className="error-message">{errors.firstName.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="lastName">Last Name:</label>
          <input
            type="text"
            id="lastName"
            {...register("lastName", { onChange: () => trigger("lastName") })}
            className={errors.lastName && touchedFields.lastName ? 'invalid' : ''}
          />
          {errors.lastName && <p className="error-message">{errors.lastName.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            {...register("emailAddress", { onChange: () => trigger("emailAddress") })}
            className={errors.emailAddress && touchedFields.emailAddress ? 'invalid' : ''}
          />
          {errors.emailAddress && <p className="error-message">{errors.emailAddress.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="phoneNumber">Phone Number:</label>
          <input
            type="tel"
            id="phoneNumber"
            {...register("phoneNumber", { onChange: () => trigger("phoneNumber") })}
            className={errors.phoneNumber && touchedFields.phoneNumber ? 'invalid' : ''}
          />
          {errors.phoneNumber && <p className="error-message">{errors.phoneNumber.message}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="birthday">Birthday:</label>
          <input
            type="date"
            id="birthday"
            {...register("birthDate", { onChange: () => trigger("birthDate") })}
            className={errors.birthDate && touchedFields.birthDate ? 'invalid' : ''}
          />
          {errors.birthDate && <p className="error-message">{errors.birthDate.message}</p>}
        </div>

        <div className="button-group">
          <button type="submit" className="submit-profile-page-button">Save</button>
          <button type="button" className="reset-profile-page-button" onClick={handleReset}>Discard</button>
        </div>
      </form>
    </div>
  );
};

export default Profile;
