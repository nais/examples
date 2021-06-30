import React, { useState, useEffect } from 'react'
import './UserInfo.css'
import extractSubject from '../../tokeninterpreter'

const UserInfo = () => {
    const [user, setUser] = useState()

    useEffect(() => {
        setUser(extractSubject(document.cookie))
    })
    
    return (
        <p className='centered'>{ (user && `User: ${user}`) || 'unknown user' }</p>
    )
}

export default UserInfo